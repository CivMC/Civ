package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class ByteParameterParser implements ParameterParser<Byte> {

	@Override
	public Class<Byte> getClassParsed() {
		return Byte.class;
	}

	@Override
	public Byte parse(String value) throws InvalidParameterValueException {
		try {
			return Byte.parseByte(value);
		} catch (NumberFormatException e) {
			throw new InvalidParameterValueException();
		}
	}
}
