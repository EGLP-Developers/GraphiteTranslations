package me.mrletsplay.gtranslations;

import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum TranslationStatus implements JSONPrimitiveStringConvertible {
	
	NOT_TRANSLATED("Not Translated", "orangered"),
	SUBMITTED("Submitted", "cornflowerblue"),
	ACCEPTED("Accepted", "limegreen");
	
	private final String
		friendlyName,
		color;
	
	private TranslationStatus(String friendlyName, String color) {
		this.friendlyName = friendlyName;
		this.color = color;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
	
	public String getColor() {
		return color;
	}

	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static TranslationStatus decodePrimitive(Object p) {
		return valueOf((String) p);
	}

}
