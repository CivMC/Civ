package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class CharParameterParser implements ParameterParser<Character> {

	@Override
	public Class<Character> getClassParsed() {
		return Character.class;
	}

	@Override
	public Character parse(String value) throws InvalidParameterValueException {
		if (value.length() != 1) {
			throw new InvalidParameterValueException();
		}
		return value.charAt(0);
	}
}
