package com.github.maxopoly.KiraBukkitGateway.impersonation;

import java.util.UUID;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.ContextManager;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;

public class KiraLuckPermsWrapper {
	
	private LuckPerms api;
	
	public KiraLuckPermsWrapper() {
		api = LuckPermsProvider.get();
	}
	
	public boolean hasPermission(UUID uuid, String permission) {
		User user = api.getUserManager().getUser(uuid);
		if (user == null) {
			return false;
		}
		ContextManager cm = api.getContextManager();
		QueryOptions queryOptions = cm.getQueryOptions(user).orElse(cm.getStaticQueryOptions());
		CachedPermissionData permissionData = user.getCachedData().getPermissionData(queryOptions);
		Tristate checkResult = permissionData.checkPermission(permission);
		return checkResult.asBoolean();
	}

}
