package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

import java.util.UUID;

public class TridentHandler {

	private boolean returnToOffhand;
	private long riptideCooldown;
	private long generalCooldown;

	private CooldownHandler riptideCooldownHandler;
	private CooldownHandler generalCooldownHandler;

	public TridentHandler(boolean returnToOffhand, long riptideCooldown, long generalCooldown) {
		this.returnToOffhand = returnToOffhand;
		this.riptideCooldown = riptideCooldown;
		this.generalCooldown = generalCooldown;

		this.riptideCooldownHandler = new CooldownHandler("riptideCooldown", riptideCooldown, (player, cooldowns) ->
				ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "Riptide: " +
						ChatColor.GREEN + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);
		this.generalCooldownHandler = new CooldownHandler("tridentCooldown", generalCooldown, (player, cooldowns) ->
				ChatColor.DARK_RED + "" + ChatColor.BOLD + "Trident: " +
						ChatColor.RED + CooldownHandler.formatCoolDown(cooldowns, player.getUniqueId())
		);
	}

	public boolean isReturnToOffhand() {
		return returnToOffhand;
	}

	public void putRiptideOnCooldown(Player shooter) {
		riptideCooldownHandler.putOnCooldown(shooter);
	}

	public void putTridentOnCooldown(Player shooter) {
		generalCooldownHandler.putOnCooldown(shooter);
	}

	public boolean isRiptideOnCooldown(Player player) {
		return riptideCooldownHandler.onCooldown(player);
	}

	public boolean isTridentOnCooldown(Player player) {
		return generalCooldownHandler.onCooldown(player);
	}

}
