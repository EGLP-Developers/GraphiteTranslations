package me.mrletsplay.gtranslations;

import me.mrletsplay.mrcore.json.converter.JSONPrimitiveStringConvertible;

public enum TranslationStatus implements JSONPrimitiveStringConvertible {
	
	NOT_TRANSLATED,
	SUBMITTED,
	ACCEPTED;
	
	@Override
	public String toJSONPrimitive() {
		return name();
	}
	
	public static TranslationStatus decodePrimitive(Object p) {
		return valueOf((String) p);
	}

}
