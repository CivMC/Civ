package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import com.google.gson.JsonObject;

public abstract class AbstractRequestHandler {
	
	private String identifier;
	
	public AbstractRequestHandler(String identifier) {
		this.identifier = identifier;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public abstract void handle(JsonObject input, JsonObject output);

}
