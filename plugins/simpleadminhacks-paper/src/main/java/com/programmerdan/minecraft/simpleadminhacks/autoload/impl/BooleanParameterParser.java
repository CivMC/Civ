package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public class BooleanParameterParser implements ParameterParser<Boolean> {

	@Override
	public Class<Boolean> getClassParsed() {
		return Boolean.class;
	}

	@Override
	public Boolean parse(String value) throws InvalidParameterValueException {
		switch (value.toLowerCase()) {
		case "1":
		case "true":
		case "t":
			return true;
		case "0":
		case "false":
		case "f":
			return false;
		case "null":
			return null;
		}
		throw new InvalidParameterValueException();
	}
}
