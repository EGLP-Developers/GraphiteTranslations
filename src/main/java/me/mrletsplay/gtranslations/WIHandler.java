package me.mrletsplay.gtranslations;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.webinterfaceapi.page.action.ActionEvent;
import me.mrletsplay.webinterfaceapi.page.action.ActionHandler;
import me.mrletsplay.webinterfaceapi.page.action.ActionResponse;
import me.mrletsplay.webinterfaceapi.page.action.WebinterfaceHandler;

public class WIHandler implements ActionHandler {

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "submitTranslation")
	public ActionResponse submitTranslation(ActionEvent event) {
		JSONObject v = event.getData();
		String locale = v.getString("locale");
		String path = v.getString("path");
		String translation = v.getString("translation");
		boolean accept = v.optBoolean("accept").orElse(false);
		if(locale == null || path == null || translation == null || translation.trim().isEmpty()) return ActionResponse.error("Missing locale/path/translation");
		
		if(accept && !event.getAccount().hasPermission("gtranslate.accept")) {
			return ActionResponse.error("No permission");
		}
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return ActionResponse.error("Unknown message");
		if(d.getStatus() == TranslationStatus.ACCEPTED) return ActionResponse.error("Translation already accepted");
		
		d.setStatus(accept ? TranslationStatus.ACCEPTED : TranslationStatus.SUBMITTED);
		d.setText(translation.trim());
		GraphiteTranslations.saveTranslationData();
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "acceptTranslation")
	public ActionResponse acceptTranslation(ActionEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.accept")) return ActionResponse.error("No permission");
		JSONObject v = event.getData();
		String locale = v.getString("locale");
		String path = v.getString("path");
		if(locale == null || path == null) return ActionResponse.error("Missing locale/path");
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return ActionResponse.error("Unknown message");
		if(d.getStatus() != TranslationStatus.SUBMITTED) return ActionResponse.error("Translation not submitted");
		if(d.getStatus() == TranslationStatus.ACCEPTED) return ActionResponse.error("Translation already accepted");
		
		d.setStatus(TranslationStatus.ACCEPTED);
		GraphiteTranslations.saveTranslationData();
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "unacceptTranslation")
	public ActionResponse unacceptTranslation(ActionEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.accept")) return ActionResponse.error("No permission");
		JSONObject v = event.getData();
		String locale = v.getString("locale");
		String path = v.getString("path");
		if(locale == null || path == null) return ActionResponse.error("Missing locale/path");
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return ActionResponse.error("Unknown message");
		if(d.getStatus() != TranslationStatus.ACCEPTED) return ActionResponse.error("Translation not accepted");
		
		d.setStatus(TranslationStatus.SUBMITTED);
		GraphiteTranslations.saveTranslationData();
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "importLocale")
	public ActionResponse importLocale(ActionEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.import-export")) return ActionResponse.error("No permission");
		String locale = event.getData().getString("locale");
		if(locale == null) return ActionResponse.error("Missing locale");
		
		GraphiteTranslations.importLocale(locale);
		GraphiteTranslations.saveTranslationData();
		return ActionResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "exportLocale")
	public ActionResponse exportLocale(ActionEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.import-export")) return ActionResponse.error("No permission");
		String locale = event.getData().getString("locale");
		if(locale == null) return ActionResponse.error("Missing locale");
		
		GraphiteTranslations.exportLocale(locale);
		GraphiteTranslations.saveTranslationData();
		return ActionResponse.success();
	}
	
}
