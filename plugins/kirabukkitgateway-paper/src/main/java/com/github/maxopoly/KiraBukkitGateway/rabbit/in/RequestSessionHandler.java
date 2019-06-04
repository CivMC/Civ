package com.github.maxopoly.KiraBukkitGateway.rabbit.in;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import com.github.maxopoly.KiraBukkitGateway.rabbit.RabbitInput;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RequestSessionHandler extends RabbitInput {

	private static final String idField = "RequestSessionId";
	private static final String keyField = "RequestSessionKey";

	public RequestSessionHandler() {
		super("requestsession");
	}

	@Override
	public void handle(JsonObject input) {
		long id = input.get(idField).getAsLong();
		String type = input.get(keyField).getAsString();
		JsonObject reply = new JsonObject();
		reply.addProperty(idField, id);
		switch (type) {
		case "permissioncheck":
			boolean hasPerm = checkPerm(input);
			reply.addProperty("hasPermission", hasPerm);
			break;
		case "apiperms":
			UUID runner = UUID.fromString(input.get("uuid").getAsString());
			JsonArray snitchArray = new JsonArray();
			getGroupsWithPermission(runner, "SNITCH_NOTIFICATIONS").forEach(g -> snitchArray.add(g));
			reply.add("snitches", snitchArray);
			JsonArray chatArray = new JsonArray();
			getGroupsWithPermission(runner, "READ_CHAT").forEach(g -> chatArray.add(g));
			reply.add("read_chat", chatArray);
			JsonArray writeChatArray = new JsonArray();
			getGroupsWithPermission(runner, "WRITE_CHAT").forEach(g -> writeChatArray.add(g));
			reply.add("write_chat", writeChatArray);
			break;
		default:
			throw new IllegalArgumentException(type + " is not a valid request");
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().replyToRequestSession(reply);
	}
	
	private Set<String> getGroupsWithPermission(UUID uuid, String permission) {
		Set<String> result = new HashSet<>();
		GroupManager gm = NameAPI.getGroupManager();
		PermissionType perm = PermissionType.getPermission(permission);
		for(String groupName : gm.getAllGroupNames(uuid)) {
			if (gm.hasAccess(groupName, uuid, perm)) {
				result.add(groupName);
			}
		}
		return result;
	}

	private boolean checkPerm(JsonObject input) {
		UUID player = UUID.fromString(input.get("player").getAsString());
		String group = input.get("group").getAsString();
		String permission = input.get("permission").getAsString();
		Group g = GroupManager.getGroup(group);
		if (g == null) {
			return false;
		}
		PermissionType perm = PermissionType.getPermission(permission);
		if (perm == null) {
			return false;
		}
		return NameAPI.getGroupManager().hasAccess(g, player, perm);
	}
}
