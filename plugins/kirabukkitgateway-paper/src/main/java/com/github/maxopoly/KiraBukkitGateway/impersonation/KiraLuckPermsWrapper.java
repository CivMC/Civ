package com.github.maxopoly.KiraBukkitGateway.impersonation;

import java.util.UUID;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;

public class KiraLuckPermsWrapper {
	
	private LuckPermsApi api;
	
	public KiraLuckPermsWrapper() {
		api = LuckPerms.getApi();
	}
	
	public boolean hasPermission(UUID uuid, String permission) {
		User user = api.getUser(uuid);
		if (user == null) {
			return false;
		}
		return user.hasPermission(api.buildNode(permission).build()).asBoolean();
	}

}
