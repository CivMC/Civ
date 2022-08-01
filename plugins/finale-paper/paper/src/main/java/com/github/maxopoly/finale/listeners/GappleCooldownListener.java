package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.FinaleSettingManager;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.function.BiFunction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.CivScoreBoard;
import vg.civcraft.mc.civmodcore.players.scoreboard.side.ScoreBoardAPI;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.utilities.cooldowns.TickCoolDownHandler;

public class GappleCooldownListener implements Listener {

	private ICoolDownHandler<UUID> coolDownHandler;
	private DecimalFormat df = new DecimalFormat("#.0");
	private CivScoreBoard cooldownBoard;
	private BottomLine cooldownBottomLine;

	public GappleCooldownListener(long cooldown) {
		this.coolDownHandler = new TickCoolDownHandler<>(Finale.getPlugin(), cooldown / 50);
	}

	@EventHandler
	public void onItemConsume(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.GOLDEN_APPLE) {
			return;
		}
		Player player = event.getPlayer();
		if (coolDownHandler.onCoolDown(player.getUniqueId())) {
			event.setCancelled(true);
			player.sendMessage(Component.text("You may eat this again in " + formatCoolDown(player.getUniqueId()) + " seconds",
				NamedTextColor.RED));
			return;
		}
		putOnCooldown(player);
	}

	public BottomLine getCooldownBottomLine() {
		if (cooldownBottomLine == null) {
			cooldownBottomLine = BottomLineAPI.createBottomLine("gappleCooldown", 1);
			cooldownBottomLine.updatePeriodically(getCooldownBiFunction(), 1L);
		}
		return cooldownBottomLine;
	}

	public CivScoreBoard getCooldownBoard() {
		if (cooldownBoard == null) {
			cooldownBoard = ScoreBoardAPI.createBoard("gappleCooldown");
			cooldownBoard.updatePeriodically(getCooldownBiFunction(), 1L);
		}
		return cooldownBoard;
	}

	public String getCooldownText(Player shooter) {
		return ChatColor.GOLD + "" + ChatColor.BOLD + "Golden Apple: " + ChatColor.GOLD + formatCoolDown(shooter.getUniqueId());
	}

	public void putOnCooldown(Player shooter) {
		coolDownHandler.putOnCoolDown(shooter.getUniqueId());
		FinaleSettingManager settings = Finale.getPlugin().getSettingsManager();
		if (settings.setVanillaItemCooldown(shooter.getUniqueId())) {
			Bukkit.getScheduler().runTask(Finale.getPlugin(), ()->{
				// -1, because this is delayed by one tick
				shooter.setCooldown(Material.GOLDEN_APPLE, (int) coolDownHandler.getTotalCoolDown());
			});
		}

		if (settings.actionBarItemCooldown(shooter.getUniqueId())) {
			BottomLine bottomLine = getCooldownBottomLine();
			bottomLine.updatePlayer(shooter, getCooldownText(shooter));
		}
		if (settings.sideBarItemCooldown(shooter.getUniqueId())) {
			CivScoreBoard board = getCooldownBoard();
			board.set(shooter, getCooldownText(shooter));
		}
	}

	public BiFunction<Player, String, String> getCooldownBiFunction() {
		return (shooter, oldText) -> {
			if (!coolDownHandler.onCoolDown(shooter.getUniqueId())) {
				return null;
			}

			return getCooldownText(shooter);
		};
	}

	private String formatCoolDown(UUID uuid) {
		long cd = coolDownHandler.getRemainingCoolDown(uuid);
		if (cd <= 0) {
			return ChatColor.GREEN + "READY";
		}
		//convert from ticks to ms
		return df.format(cd / 20.0) + " sec";
	}
}
