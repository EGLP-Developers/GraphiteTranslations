package me.mrletsplay.gtranslations;

import me.mrletsplay.mrcore.json.JSONObject;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceActionHandler;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceHandler;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceRequestEvent;
import me.mrletsplay.webinterfaceapi.webinterface.page.action.WebinterfaceResponse;

public class WIHandler implements WebinterfaceActionHandler {

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "submitTranslation")
	public WebinterfaceResponse submitTranslation(WebinterfaceRequestEvent event) {
		JSONObject v = event.getRequestData().getJSONObject("value");
		String locale = v.getString("locale");
		String path = v.getString("path");
		String translation = v.getString("translation");
		boolean accept = v.optBoolean("accept").orElse(false);
		if(locale == null || path == null || translation == null || translation.trim().isEmpty()) return WebinterfaceResponse.error("Missing locale/path/translation");
		
		if(accept && !event.getAccount().hasPermission("gtranslate.accept")) {
			return WebinterfaceResponse.error("No permission");
		}
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return WebinterfaceResponse.error("Unknown message");
		if(d.getStatus() == TranslationStatus.ACCEPTED) return WebinterfaceResponse.error("Translation already accepted");
		
		d.setStatus(accept ? TranslationStatus.ACCEPTED : TranslationStatus.SUBMITTED);
		d.setText(translation.trim());
		GraphiteTranslations.saveTranslationData();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "acceptTranslation")
	public WebinterfaceResponse acceptTranslation(WebinterfaceRequestEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.accept")) return WebinterfaceResponse.error("No permission");
		JSONObject v = event.getRequestData().getJSONObject("value");
		String locale = v.getString("locale");
		String path = v.getString("path");
		if(locale == null || path == null) return WebinterfaceResponse.error("Missing locale/path");
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return WebinterfaceResponse.error("Unknown message");
		if(d.getStatus() != TranslationStatus.SUBMITTED) return WebinterfaceResponse.error("Translation not submitted");
		if(d.getStatus() == TranslationStatus.ACCEPTED) return WebinterfaceResponse.error("Translation already accepted");
		
		d.setStatus(TranslationStatus.ACCEPTED);
		GraphiteTranslations.saveTranslationData();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "unacceptTranslation")
	public WebinterfaceResponse unacceptTranslation(WebinterfaceRequestEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.accept")) return WebinterfaceResponse.error("No permission");
		JSONObject v = event.getRequestData().getJSONObject("value");
		String locale = v.getString("locale");
		String path = v.getString("path");
		if(locale == null || path == null) return WebinterfaceResponse.error("Missing locale/path");
		
		TranslationData d = GraphiteTranslations.getTranslationData().stream()
			.filter(t -> t.getLocaleIdentifier().equals(locale) && t.getPath().equals(path))
			.findFirst().orElse(null);
		if(d == null) return WebinterfaceResponse.error("Unknown message");
		if(d.getStatus() != TranslationStatus.ACCEPTED) return WebinterfaceResponse.error("Translation not accepted");
		
		d.setStatus(TranslationStatus.SUBMITTED);
		GraphiteTranslations.saveTranslationData();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "importLocale")
	public WebinterfaceResponse importLocale(WebinterfaceRequestEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.import-export")) return WebinterfaceResponse.error("No permission");
		String locale = event.getRequestData().getString("value");
		if(locale == null) return WebinterfaceResponse.error("Missing locale");
		
		GraphiteTranslations.importLocale(locale);
		GraphiteTranslations.saveTranslationData();
		return WebinterfaceResponse.success();
	}

	@WebinterfaceHandler(requestTarget = "gtranslations", requestTypes = "exportLocale")
	public WebinterfaceResponse exportLocale(WebinterfaceRequestEvent event) {
		if(!event.getAccount().hasPermission("gtranslate.import-export")) return WebinterfaceResponse.error("No permission");
		String locale = event.getRequestData().getString("value");
		if(locale == null) return WebinterfaceResponse.error("Missing locale");
		
		GraphiteTranslations.exportLocale(locale);
		GraphiteTranslations.saveTranslationData();
		return WebinterfaceResponse.success();
	}
	
}
