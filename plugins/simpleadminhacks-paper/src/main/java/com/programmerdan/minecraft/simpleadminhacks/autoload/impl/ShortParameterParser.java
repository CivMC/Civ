package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class ShortParameterParser implements ParameterParser<Short> {

	@Override
	public Class<Short> getClassParsed() {
		return Short.class;
	}

	@Override
	public Short parse(String value) throws InvalidParameterValueException {
		try {
			return Short.parseShort(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
