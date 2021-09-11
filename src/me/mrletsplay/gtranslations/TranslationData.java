package me.mrletsplay.gtranslations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class TranslationData implements JSONConvertible {
	
	private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{(?<name>.+?)\\}");
	
	@JSONValue
	private String localeIdentifier;
	
	@JSONValue
	private String path;
	
	@JSONValue
	private String text;

	@JSONValue
	private TranslationStatus status;
	
	private MessageInfo messageInfo;
	
	@JSONConstructor
	private TranslationData() {}
	
	public TranslationData(String localeIdentifier, String path, String text, TranslationStatus status) {
		this.localeIdentifier = localeIdentifier;
		this.path = path;
		this.text = text;
		this.status = status;
	}

	public String getLocaleIdentifier() {
		return localeIdentifier;
	}

	public String getPath() {
		return path;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
	public void setStatus(TranslationStatus status) {
		this.status = status;
	}

	public TranslationStatus getStatus() {
		return status;
	}
	
	public void setMessageInfo(MessageInfo messageInfo) {
		this.messageInfo = messageInfo;
	}
	
	public MessageInfo getMessageInfo() {
		return messageInfo;
	}
	
	public List<String> checkErrors() {
		if(text == null) return Collections.emptyList();
		
		List<String> chs = new ArrayList<>();
		
		List<String> translationPlaceholders = new ArrayList<>();
		

		Matcher m = PLACEHOLDER_PATTERN.matcher(text);
		while(m.find()) {
			String ph = m.group("name");
			if(ph.startsWith("emote_")) continue; // Ignore JDAEmotes
			translationPlaceholders.add(ph);
			if(!messageInfo.getPlaceholders().contains(ph)) {
				chs.add("Unknown placeholder: " + ph);
			}
		}

		for(String oph : messageInfo.getPlaceholders()) {
			if(!translationPlaceholders.contains(oph)) {
				chs.add("Missing placeholder: " + oph);
			}
		}
		
		return chs;
	}
	
}
