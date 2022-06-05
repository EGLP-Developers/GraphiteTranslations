package me.mrletsplay.gtranslations;

import java.io.File;

import me.mrletsplay.mrcore.config.ConfigLoader;
import me.mrletsplay.mrcore.config.FileCustomConfig;

public class GraphiteLocale {
	
	private String localeIdentifier;
	private FileCustomConfig config;
	
	public GraphiteLocale(String localeIdentifier) {
		this.localeIdentifier = localeIdentifier;
		this.config = ConfigLoader.loadFileConfig(new File("locales/" + localeIdentifier + ".yml"));
	}
	
	public String getLocaleIdentifier() {
		return localeIdentifier;
	}
	
	public String getMessage(String path) {
		return config.getString(path);
	}
	
	public void setMessage(String path, String message) {
		config.set(path, message);
	}

}
