package com.github.maxopoly.KiraBukkitGateway.rabbit.request;

import java.util.UUID;

import com.google.gson.JsonObject;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class PermissionCheckHandler extends AbstractRequestHandler {

	public PermissionCheckHandler() {
		super("permissioncheck");
	}

	@Override
	public void handle(JsonObject input, JsonObject output) {
		boolean hasPerm = checkPerm(input);
		output.addProperty("hasPermission", hasPerm);
	}
	
	private static boolean checkPerm(JsonObject input) {
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
