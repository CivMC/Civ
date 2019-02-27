package com.github.maxopoly.KiraBukkitGateway;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.permission.Permission;

public class VaultAPI {

	private Permission perms;

	public VaultAPI() {
		if (!setupPermissions()) {
			throw new IllegalStateException("Failed to setup permissions");
		}
	}

	public boolean hasPermission(PseudoPlayer player, String permission) {
		return perms.playerHas("world", player.getOfflinePlayer(), permission);
	}

	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager()
				.getRegistration(Permission.class);
		perms = rsp.getProvider();
		return perms != null;
	}

}
