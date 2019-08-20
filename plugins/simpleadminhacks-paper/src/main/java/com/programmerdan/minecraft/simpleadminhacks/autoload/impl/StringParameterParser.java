package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class StringParameterParser implements ParameterParser<String> {

	@Override
	public Class<String> getClassParsed() {
		return String.class;
	}

	@Override
	public String parse(String value) {
		return value;
	}
}