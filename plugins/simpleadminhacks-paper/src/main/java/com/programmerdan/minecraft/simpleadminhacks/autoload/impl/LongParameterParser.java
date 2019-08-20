package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class LongParameterParser implements ParameterParser<Long> {

	@Override
	public Class<Long> getClassParsed() {
		return Long.class;
	}

	@Override
	public Long parse(String value) throws InvalidParameterValueException {
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
