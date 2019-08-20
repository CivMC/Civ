package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class FloatParameterParser implements ParameterParser<Float> {

	@Override
	public Class<Float> getClassParsed() {
		return Float.class;
	}

	@Override
	public Float parse(String value) throws InvalidParameterValueException {
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
