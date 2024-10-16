package com.github.maxopoly.finale.misc;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.BiFunction;

public class CooldownHandler {

	private String identifier;
	private ICoolDownHandler<UUID> cooldowns;
	private BiFunction<Player, ICoolDownHandler<UUID>, String> getCooldownText;
	private long cooldown;

	public CooldownHandler(String identifier, long cooldown, BiFunction<Player, ICoolDownHandler<UUID>, String> getCooldownText) {
		this.identifier = identifier;
		this.getCooldownText = getCooldownText;
		this.cooldown = cooldown;

		this.cooldowns = new TickCoolDownHandler<>(Finale.getPlugin(), cooldown / 50);
	}

	public long getCooldown() {
		return cooldown;
	}

	public void quit(Player player) {
		cooldowns.removeCooldown(player.getUniqueId());
	}

	public boolean onCooldown(Player player) {
		return cooldowns.onCoolDown(player.getUniqueId());
	}

	public BiFunction<Player, String, String> getCooldownBiFunction() {
		return (shooter, oldText) -> {
			if (!cooldowns.onCoolDown(shooter.getUniqueId())) {
				return null;
			}

			return getCooldownText.apply(shooter, cooldowns);
		};
	}

	private BottomLine cooldownBottomLine;

	public BottomLine getCooldownBottomLine() {
		if (cooldownBottomLine == null) {
			cooldownBottomLine = BottomLineAPI.createBottomLine(identifier, 1);
			cooldownBottomLine.updatePeriodically(getCooldownBiFunction(), 1L);
		}
		return cooldownBottomLine;
	}

	private CivScoreBoard cooldownBoard;

	public CivScoreBoard getCooldownBoard() {
		if (cooldownBoard == null) {
			cooldownBoard = ScoreBoardAPI.createBoard(identifier);
			cooldownBoard.updatePeriodically(getCooldownBiFunction(), 1L);
		}
		return cooldownBoard;
	}

	public void putOnCooldown(Player shooter) {
		cooldowns.putOnCoolDown(shooter.getUniqueId());
		FinaleSettingManager settings = Finale.getPlugin().getSettingsManager();
		if (settings.vanillaTimewarpCooldown(shooter.getUniqueId())) {
			Bukkit.getScheduler().runTaskLater(Finale.getPlugin(), () -> {
				// -1, because this is delayed by one tick
				shooter.setCooldown(Material.CHORUS_FRUIT, (int) cooldowns.getTotalCoolDown() - 1);
			}, 1);
		}

		if (settings.actionBarTimewarpCooldown(shooter.getUniqueId())) {
			BottomLine bottomLine = getCooldownBottomLine();
			bottomLine.updatePlayer(shooter, getCooldownText.apply(shooter, cooldowns));
		}
		if (settings.sideBarTimewarpCooldown(shooter.getUniqueId())) {
			CivScoreBoard board = getCooldownBoard();
			board.set(shooter, getCooldownText.apply(shooter, cooldowns));
		}
	}

	private static final DecimalFormat df = new DecimalFormat("#.0");

	public static String formatCoolDown(ICoolDownHandler<UUID> cooldowns, UUID uuid) {
		long cd = cooldowns.getRemainingCoolDown(uuid);
		if (cd <= 0) {
			return ChatColor.GREEN + "READY";
		}
		//convert from ticks to ms
		return df.format(cd / 20.0) + " sec";
	}

}
