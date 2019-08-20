package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class DoubleParameterParser implements ParameterParser<Double> {

	@Override
	public Class<Double> getClassParsed() {
		return Double.class;
	}

	@Override
	public Double parse(String value) throws InvalidParameterValueException {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
