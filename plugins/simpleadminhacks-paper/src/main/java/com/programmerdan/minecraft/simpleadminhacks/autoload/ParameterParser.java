package com.programmerdan.minecraft.simpleadminhacks.autoload;

public interface ParameterParser<T> {

	Class<T> getClassParsed();

	T parse(String value) throws InvalidParameterValueException;

}
