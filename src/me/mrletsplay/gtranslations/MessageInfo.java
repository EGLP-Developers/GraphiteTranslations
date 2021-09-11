package me.mrletsplay.gtranslations;

import java.util.List;

import me.mrletsplay.mrcore.json.JSONType;
import me.mrletsplay.mrcore.json.converter.JSONConstructor;
import me.mrletsplay.mrcore.json.converter.JSONConvertible;
import me.mrletsplay.mrcore.json.converter.JSONListType;
import me.mrletsplay.mrcore.json.converter.JSONValue;

public class MessageInfo implements JSONConvertible {
	
	@JSONValue
	private String path;
	
	@JSONValue("enum_name")
	private String enumName;

	@JSONValue
	private String fallback;
	
	@JSONValue("referenced_by")
	@JSONListType(JSONType.STRING)
	private List<String> referencedBy;
	
	@JSONValue("special_roles")
	@JSONListType(JSONType.STRING)
	private List<String> specialRoles;
	
	@JSONValue
	@JSONListType(JSONType.STRING)
	private List<String> placeholders;
	
	@JSONConstructor
	private MessageInfo() {}

	public String getPath() {
		return path;
	}

	public String getEnumName() {
		return enumName;
	}

	public String getFallback() {
		return fallback;
	}

	public List<String> getReferencedBy() {
		return referencedBy;
	}

	public List<String> getSpecialRoles() {
		return specialRoles;
	}
	
	public List<String> getPlaceholders() {
		return placeholders;
	}
	
}
