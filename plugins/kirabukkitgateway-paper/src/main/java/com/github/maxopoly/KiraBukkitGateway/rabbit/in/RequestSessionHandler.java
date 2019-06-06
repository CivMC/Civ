package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import java.util.Map;
import java.util.TreeMap;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.github.maxopoly.KiraBukkitGateway.rabbit.request.AbstractRequestHandler;
import com.github.maxopoly.KiraBukkitGateway.rabbit.request.ApiPermsHandler;
import com.github.maxopoly.KiraBukkitGateway.rabbit.request.ConsoleCommandHandler;
import com.github.maxopoly.KiraBukkitGateway.rabbit.request.PermissionCheckHandler;
import com.google.gson.JsonObject;

public class RequestSessionHandler extends RabbitInput {

	private static final String idField = "RequestSessionId";
	private static final String keyField = "RequestSessionKey";
	
	private Map<String, AbstractRequestHandler> handlers;

	public RequestSessionHandler() {
		super("requestsession");
		registerHandlers();
	}
	
	private void registerHandlers() {
		handlers = new TreeMap<>();
		registerHandler(new ConsoleCommandHandler());
		registerHandler(new PermissionCheckHandler());
		registerHandler(new ApiPermsHandler());
	}
	
	private void registerHandler(AbstractRequestHandler handler) {
		handlers.put(handler.getIdentifier(), handler);
	}
 
	@Override
	public void handle(JsonObject input) {
		long id = input.get(idField).getAsLong();
		String type = input.get(keyField).getAsString();
		JsonObject reply = new JsonObject();
		reply.addProperty(idField, id);
		AbstractRequestHandler handler = handlers.get(type);
		if (handler == null) {
			throw new IllegalArgumentException(type + " is not a valid request");
		}
		handler.handle(input, reply);
		KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToRequestSession(reply);
	}
	
	
	
}
