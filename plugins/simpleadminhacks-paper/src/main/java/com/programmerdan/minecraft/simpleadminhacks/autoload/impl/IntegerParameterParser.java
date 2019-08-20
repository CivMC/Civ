package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class IntegerParameterParser implements ParameterParser<Integer> {

	@Override
	public Class<Integer> getClassParsed() {
		return Integer.class;
	}

	@Override
	public Integer parse(String value) throws InvalidParameterValueException {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
