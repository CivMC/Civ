package com.programmerdan.minecraft.simpleadminhacks.autoload.impl;

import com.programmerdan.minecraft.simpleadminhacks.autoload.InvalidParameterValueException;
import com.programmerdan.minecraft.simpleadminhacks.autoload.ParameterParser;

public abstract class AbstractObjectParameterParser<T> implements ParameterParser<T> {

	@Override
	public T parse(String value) throws InvalidParameterValueException {
		String cleaned = value.trim().toLowerCase();
		if (cleaned.equals("null")) {
			return null;
		}
		return parseObject(cleaned);
	}

	public abstract T parseObject(String value) throws InvalidParameterValueException;
}
