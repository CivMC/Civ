package com.github.devotedmc.hiddenore;

import java.util.HashMap;
import java.util.Map;

public class NameConfig {
	public String prettyName;
	private Map<Short, String> subtypePrettyName;

	public NameConfig(String prettyName) {
		this.prettyName = prettyName;
		this.subtypePrettyName = new HashMap<Short, String>();
	}

	public void addSubTypePrettyName(short subtype, String name) {
		this.subtypePrettyName.put(subtype, name);
	}

	public String getSubTypePrettyName(Short subtype) {
		if (subtype == null) {
			return prettyName;
		}
		String pName = subtypePrettyName.get(subtype);
		return pName == null ? prettyName : pName;
	}
}
