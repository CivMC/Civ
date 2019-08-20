package com.programmerdan.minecraft.simpleadminhacks.autoload;

import java.util.HashMap;
import java.util.Map;

import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.BlockFaceParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.BooleanParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.ByteParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.CharParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.DoubleParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.FloatParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.IntegerParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.LocationParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.LongParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.ShortParameterParser;
import com.programmerdan.minecraft.simpleadminhacks.autoload.impl.StringParameterParser;

public class ParameterParsingFactory {

	private Map<Class<?>, ParameterParser<?>> parserMap;
	private Map<Class<?>, Class<?>> wrapperClasses;

	public ParameterParsingFactory() {
		loadParser();
	}

	@SuppressWarnings("unchecked")
	private <T> Class<T> convertPrimitive(Class<T> primType) {
		return (Class<T>) wrapperClasses.get(primType);
	}

	@SuppressWarnings("unchecked")
	public <T> ParameterParser<T> getParser(Class<T> classType) {
		if (classType.isPrimitive()) {
			classType = convertPrimitive(classType);
		}
		ParameterParser<?> parser = parserMap.get(classType);
		if (parser == null) {
			// need to do this explicitly, otherwise cast will throw exception
			return null;
		}
		return (ParameterParser<T>) parser;
	}

	private void loadParser() {
		parserMap = new HashMap<>();
		readyParser(new BlockFaceParameterParser());
		readyParser(new BooleanParameterParser());
		readyParser(new ByteParameterParser());
		readyParser(new CharParameterParser());
		readyParser(new DoubleParameterParser());
		readyParser(new FloatParameterParser());
		readyParser(new IntegerParameterParser());
		readyParser(new LocationParameterParser());
		readyParser(new LongParameterParser());
		readyParser(new ShortParameterParser());
		readyParser(new StringParameterParser());
		wrapperClasses = new HashMap<>();
		wrapperClasses.put(boolean.class, Boolean.class);
		wrapperClasses.put(byte.class, Byte.class);
		wrapperClasses.put(char.class, Character.class);
		wrapperClasses.put(double.class, Double.class);
		wrapperClasses.put(float.class, Float.class);
		wrapperClasses.put(int.class, Integer.class);
		wrapperClasses.put(long.class, Long.class);
		wrapperClasses.put(short.class, Short.class);
		wrapperClasses.put(void.class, Void.class);
	}

	private void readyParser(ParameterParser<?> parParser) {
		this.parserMap.put(parParser.getClassParsed(), parParser);
	}

}
