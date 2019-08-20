package com.programmerdan.minecraft.simpleadminhacks;

public class InvalidConfigException extends RuntimeException {

	private static final long serialVersionUID = 7614429135140646756L;

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(String message, Throwable source) {
		super(message, source);
	}
}

