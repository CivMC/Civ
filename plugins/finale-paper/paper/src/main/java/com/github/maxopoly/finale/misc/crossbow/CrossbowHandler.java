package com.github.maxopoly.finale.misc.crossbow;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.CooldownHandler;
import com.github.maxopoly.finale.misc.ItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

public class CrossbowHandler {

	private boolean enabled;
	private double reinforcementDamage;
	private double bastionDamage;

	private Map<String, AntiAirMissile> antiAirMissiles;

	private CooldownHandler cooldowns;

	public CrossbowHandler(boolean enabled, double reinforcementDamage, double bastionDamage, long cooldown, Map<String, AntiAirMissile> antiAirMissiles) {
		this.enabled = enabled;
		this.reinforcementDamage = reinforcementDamage;
		this.bastionDamage = bastionDamage;

		this.antiAirMissiles = antiAirMissiles;

		this.cooldowns = new CooldownHandler("crossbowCooldown", cooldown, (player, cooldowns) ->
				ChatColor.DARK_RED + "" + ChatColor.BOLD + "Crossbow: " +
						ChatColor.RED + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);

		new BukkitRunnable() {

			@Override
			public void run() {
				for (AntiAirMissile antiAirMissile : antiAirMissiles.values()) {
					antiAirMissile.progressInstances();
				}
			}

		}.runTaskTimer(Finale.getPlugin(), 0L, 1L);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public double getReinforcementDamage() {
		return reinforcementDamage;
	}

	public double getBastionDamage() {
		return bastionDamage;
	}

	public boolean onCooldown(Player shooter) {
		return cooldowns.onCooldown(shooter);
	}

	public void putOnCooldown(Player shooter) {
		cooldowns.putOnCooldown(shooter);
	}

	public AntiAirMissile getAntiAirMissile(ItemStack is) {
		String key = ItemUtil.getAAKey(is);
		return getAntiAirMissile(key);
	}

	public AntiAirMissile getAntiAirMissile(String key) {
		return antiAirMissiles.get(key);
	}

}
