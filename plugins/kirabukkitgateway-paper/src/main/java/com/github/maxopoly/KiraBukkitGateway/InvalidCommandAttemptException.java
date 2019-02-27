package com.github.maxopoly.KiraBukkitGateway;

public class InvalidCommandAttemptException extends RuntimeException {

	private static final long serialVersionUID = -7864555241340731587L;

	public InvalidCommandAttemptException() {
		super();
	}

	public InvalidCommandAttemptException(String msg) {
		super(msg);
	}

}
