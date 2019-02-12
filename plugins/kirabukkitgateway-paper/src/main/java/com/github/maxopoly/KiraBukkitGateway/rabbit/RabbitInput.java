package com.github.maxopoly.KiraBukkitGateway.rabbit;

import com.google.gson.JsonObject;

public abstract class RabbitInput {
	
	private String identifier;

	public RabbitInput(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public abstract void handle(JsonObject input);

}
