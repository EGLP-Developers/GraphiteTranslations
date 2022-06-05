package me.mrletsplay.gtranslations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import me.mrletsplay.gtranslations.page.ImportExportPage;
import me.mrletsplay.gtranslations.page.NextTranslationDocument;
import me.mrletsplay.gtranslations.page.TranslatePage;
import me.mrletsplay.gtranslations.page.TranslationsPage;
import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.ConfigValueType;
import me.mrletsplay.mrcore.config.FileCustomConfig;
import me.mrletsplay.mrcore.io.IOUtils;
import me.mrletsplay.mrcore.json.JSONArray;
import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.mrcore.json.converter.JSONConverter;
import me.mrletsplay.mrcore.json.converter.SerializationOption;
import me.mrletsplay.mrcore.misc.FriendlyException;
import me.mrletsplay.webinterfaceapi.Webinterface;
import me.mrletsplay.webinterfaceapi.page.PageCategory;

public class GraphiteTranslations {
	
	private static List<MessageInfo> messages;
	private static List<TranslationData> translationData;
	
	public static final List<String> LOCALES = Arrays.asList(
			"de"
		);
	
	public static void main(String[] args) {
		loadMessages();
		loadTranslationData();
		
//		importLocale("de");
		
		AtomicBoolean translationsChangedOnLoad = new AtomicBoolean();
		translationsChangedOnLoad.set(translationData.removeIf(t -> t.getMessageInfo() == null));
		boolean b = translationData.removeIf(t -> !LOCALES.contains(t.getLocaleIdentifier()));
		if(b) translationsChangedOnLoad.set(true);
		
		for(String locale : LOCALES) {
			messages.forEach(m -> {
				TranslationData d = translationData.stream().filter(t -> t.getPath().equals(m.getPath()) && t.getLocaleIdentifier().equals(locale)).findFirst().orElse(null);
				if(d == null) {
					TranslationData dt = new TranslationData(locale, m.getPath(), null, TranslationStatus.NOT_TRANSLATED);
					dt.setMessageInfo(m);
					translationData.add(dt);
					translationsChangedOnLoad.set(true);
				}
			});
		}
		
		if(translationsChangedOnLoad.get()) {
			try {
				File t = new File("translation-data.json");
				if(t.exists()) {
					System.out.println("Translations changed on load, copying translation-data.json before saving...");
					Files.copy(t.toPath(), new File("translation-data-" + System.currentTimeMillis() + ".json").toPath());
				}
			} catch (IOException e) {
				throw new FriendlyException(e);
			}
			saveTranslationData();
		}
		
		Webinterface.registerActionHandler(new WIHandler());
		
		PageCategory cat = Webinterface.createCategory("Translate");
		
		cat.addPage(new TranslationsPage());
		cat.addPage(new TranslatePage());
		cat.addPage(new ImportExportPage());
		
		Webinterface.start();
		Webinterface.extractResources("/gtranslations-resources.list");
		
		Webinterface.getDocumentProvider().registerDocument("/t/next", new NextTranslationDocument());
	}
	
	private static void loadMessages() {
		System.out.println("Loading messages...");
		messages = new ArrayList<>();
		try(FileInputStream fIn = new FileInputStream(new File("locale-descriptor.json"))) {
			JSONArray msgs = new JSONArray(new String(IOUtils.readAllBytes(fIn), StandardCharsets.UTF_8));
			for(Object o : msgs) {
				messages.add(JSONConverter.decodeObject((JSONObject) o, MessageInfo.class));
			}
			Collections.sort(messages, Comparator.comparing(m -> m.getPath()));
			System.out.println("Done!");
		}catch(IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	private static void loadTranslationData() {
		System.out.println("Loading translations...");
		translationData = new ArrayList<>();
		File tD = new File("translation-data.json");
		if(!tD.exists()) {
			System.out.println("Nothing to load!");
			return;
		}
		try(FileInputStream fIn = new FileInputStream(tD)) {
			JSONArray msgs = new JSONArray(new String(IOUtils.readAllBytes(fIn), StandardCharsets.UTF_8));
			for(Object o : msgs) {
				TranslationData d = JSONConverter.decodeObject((JSONObject) o, TranslationData.class);
				d.setMessageInfo(messages.stream().filter(m -> m.getPath().equals(d.getPath())).findFirst().orElse(null));
				translationData.add(d);
			}
			Collections.sort(translationData, Comparator.comparing(m -> m.getPath()));
			System.out.println("Done!");
		}catch(IOException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static void saveTranslationData() {
		IOUtils.writeBytes(new File("translation-data.json"), new JSONArray(translationData.stream()
				.map(t -> t.toJSON(SerializationOption.DONT_INCLUDE_CLASS))
				.collect(Collectors.toList())).toFancyString().getBytes(StandardCharsets.UTF_8));
	}
	
	public static List<MessageInfo> getMessages() {
		return messages;
	}
	
	public static List<TranslationData> getTranslationData() {
		return translationData;
	}
	
	public static void exportLocale(String localeIdentifier) {
		FileCustomConfig config = ConfigLoader.loadFileConfig(new File("export/" + localeIdentifier + ".yml"));
		translationData.stream()
			.filter(t -> t.getLocaleIdentifier().equals(localeIdentifier) && t.getStatus() == TranslationStatus.ACCEPTED)
			.forEach(t -> config.set(t.getPath(), t.getText()));
		config.saveToFile();
	}
	
	public static void importLocale(String localeIdentifier) {
		System.out.println("Importing locale " + localeIdentifier + "...");
		FileCustomConfig config = ConfigLoader.loadFileConfig(new File("import/" + localeIdentifier + ".yml"));
		config.getKeys(true, true).forEach(k -> {
			if(config.getTypeOf(k) != ConfigValueType.STRING) return;
			
//			MessageInfo inf = messages.stream()
//					.filter(t -> t.getPath().equals(k))
//					.findFirst().orElse(null);
//			
//			if(inf == null) return;
			
			TranslationData dt = translationData.stream()
					.filter(t -> t.getLocaleIdentifier().equals(localeIdentifier) && t.getPath().equals(k))
					.findFirst().orElse(null);
			if(dt == null) {
				System.out.println("Skipping " + k);
//				dt = new TranslationData(localeIdentifier, k, config.getString(k), TranslationStatus.SUBMITTED);
//				dt.setMessageInfo(inf);
//				translationData.add(dt);
				return;
			}
			
			if(dt.getStatus() == TranslationStatus.NOT_TRANSLATED) {
				dt.setStatus(TranslationStatus.SUBMITTED);
				dt.setText(config.getString(k));
			}
		});
		saveTranslationData();
	}

}
