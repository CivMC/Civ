package com.github.maxopoly.finale.misc;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GappleHandler {

	private boolean enabled;
	private CooldownHandler gappleCooldowns;

	public GappleHandler(boolean enabled, long gappleCooldown) {
		this.enabled = enabled;
		gappleCooldowns = new CooldownHandler("gappleCooldown", gappleCooldown, (player, cooldowns) ->
				ChatColor.GOLD + "" + ChatColor.BOLD + "Gapple: " +
						ChatColor.YELLOW + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);
	}

	public boolean onCooldown(Player player) {
		if (!enabled) {
			return false;
		}
		return gappleCooldowns.onCooldown(player);
	}

	public void putOnCooldown(Player player) {
		if (!enabled) {
			return;
		}
		gappleCooldowns.putOnCooldown(player);
	}

}
