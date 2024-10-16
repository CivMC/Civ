package com.programmerdan.minecraft.simpleadminhacks.framework.exceptions;

public class InvalidConfigException extends RuntimeException {

	private static final long serialVersionUID = 7614429135140646756L;

	public InvalidConfigException(final String message) {
		super(message);
	}

	public InvalidConfigException(final String message, final Throwable source) {
		super(message, source);
	}

}

