package com.untamedears.JukeAlert.external;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import org.kitteh.vanish.VanishPlugin;

public class VanishNoPacket {

	private PluginManager pluginManager_;

	private VanishPlugin vanishPlugin_ = null;

	public VanishNoPacket() {

		pluginManager_ = Bukkit.getPluginManager();
	}

	public boolean isEnabled() {

		if (vanishPlugin_ != null) {
			return true;
		}
		vanishPlugin_ = (VanishPlugin) pluginManager_.getPlugin("VanishNoPacket");
		return vanishPlugin_ != null;
	}

	public boolean isPlayerVisible(Player player) {

		if (!isEnabled()) {
			return true;
		}
		return !vanishPlugin_.getManager().isVanished(player);
	}

	public boolean isPlayerInvisible(Player player) {

		if (!isEnabled()) {
			return false;
		}
		return vanishPlugin_.getManager().isVanished(player);
	}
}
