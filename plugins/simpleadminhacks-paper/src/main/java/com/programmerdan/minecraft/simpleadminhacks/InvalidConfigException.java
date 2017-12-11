package com.programmerdan.minecraft.simpleadminhacks;

public class InvalidConfigException extends RuntimeException {

	public InvalidConfigException(String message) {
		super(message);
	}

	public InvalidConfigException(String message, Throwable source) {
		super(message, source);
	}
}

