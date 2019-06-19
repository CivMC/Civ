package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ApiPermsHandler extends AbstractRequestHandler {

	public ApiPermsHandler() {
		super("apiperms", false);
	}

	@Override
	public void handle(JsonObject input, JsonObject output, long channelId) {
		UUID runner = UUID.fromString(input.get("uuid").getAsString());
		JsonArray snitchArray = new JsonArray();
		getGroupsWithPermission(runner, "SNITCH_NOTIFICATIONS").forEach(g -> snitchArray.add(g));
		output.add("snitches", snitchArray);
		JsonArray chatArray = new JsonArray();
		getGroupsWithPermission(runner, "READ_CHAT").forEach(g -> chatArray.add(g));
		output.add("read_chat", chatArray);
		JsonArray writeChatArray = new JsonArray();
		getGroupsWithPermission(runner, "WRITE_CHAT").forEach(g -> writeChatArray.add(g));
		output.add("write_chat", writeChatArray);
	}
	
	private static Set<String> getGroupsWithPermission(UUID uuid, String permission) {
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


}
