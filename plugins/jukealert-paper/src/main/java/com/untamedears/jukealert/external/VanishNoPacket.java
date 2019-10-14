package com.untamedears.jukealert.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.kitteh.vanish.VanishPlugin;

public class VanishNoPacket {

	private PluginManager pluginManager;

	private VanishPlugin vanishPlugin;

	public VanishNoPacket() {
		pluginManager = Bukkit.getPluginManager();
	}

	public boolean isEnabled() {
		if (vanishPlugin != null) {
			return true;
		}
		vanishPlugin = (VanishPlugin) pluginManager.getPlugin("VanishNoPacket");
		return vanishPlugin != null;
	}

	public boolean isPlayerInvisible(Player player) {
		if (!isEnabled()) {
			return false;
		}
		return vanishPlugin.getManager().isVanished(player);
	}

	public boolean isPlayerVisible(Player player) {
		if (!isEnabled()) {
			return true;
		}
		return !vanishPlugin.getManager().isVanished(player);
	}
}
