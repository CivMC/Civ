package com.github.maxopoly.KiraBukkitGateway.rabbit;

import java.util.Collection;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class RabbitCommands {

	private RabbitHandler internal;

	public RabbitCommands(RabbitHandler internalRabbit) {
		this.internal = internalRabbit;
	}

	public void sendAuthCode(String code, String playerName, UUID playerUUID) {
		if (code == null || playerName == null || playerUUID == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("uuid", playerUUID.toString());
		json.addProperty("name", playerName);
		json.addProperty("code", code);
		sendInternal("addauth", json);
	}

	public void sendGroupChatMessage(String group, String sender, String msg) {
		if (group == null || sender == null || msg == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("sender", sender);
		json.addProperty("msg", msg);
		sendInternal("groupchatmessage", json);
	}

	public void createGroupChatChannel(String group, Collection<UUID> members, UUID creator) {
		if (group == null || members == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("creator", creator.toString());
		JsonArray array = new JsonArray();
		members.forEach(uuid -> array.add(uuid.toString()));
		json.add("members", array);
		sendInternal("creategroupchat", json);
	}
	
	public void deleteGroupChatChannel(String group, UUID sender) {
		if (group == null || sender == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("sender", sender.toString());
		sendInternal("deletegroupchat", json);
	}
	
	public void removeGroupMember(String group, UUID member) {
		if (group == null || member == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("member", member.toString());
		sendInternal("removegroupmember", json);
	}
	
	public void addGroupMember(String group, UUID member) {
		if (group == null || member == null) {
			throw new IllegalArgumentException("Arguments cant be null");
		}
		JsonObject json = new JsonObject();
		json.addProperty("group", group);
		json.addProperty("member", member.toString());
		sendInternal("addgroupmember", json);
	}
	
	private void sendInternal(String id, JsonObject json) {
		Gson gson = new Gson();
		String payload = gson.toJson(json);
		internal.sendMessage(id + " " + payload);
	}

}
