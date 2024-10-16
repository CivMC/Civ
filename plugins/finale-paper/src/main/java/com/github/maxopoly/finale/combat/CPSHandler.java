package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.google.common.collect.Sets;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLine;
import vg.civcraft.mc.civmodcore.players.scoreboard.bottom.BottomLineAPI;

public class CPSHandler implements Listener {

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		playerClicks.remove(player.getUniqueId());
		showCps.remove(player);
	}

	private Map<UUID, ArrayDeque<Long>> playerClicks = new ConcurrentHashMap<>();
	private Set<Player> showCps = Sets.newConcurrentHashSet();

	public CPSHandler() {
		Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
	}

	public int getCPS(UUID uuid) {
		ArrayDeque<Long> clicks = this.playerClicks.get(uuid);
		if (clicks == null) {
			return 0;
		}

		final long cpsCounterInterval = Finale.getPlugin().getManager().getCombatConfig().getCpsCounterInterval();
		final long bottomTime = System.currentTimeMillis() - cpsCounterInterval;

		synchronized (clicks) {
			while (!clicks.isEmpty() && clicks.peek() < bottomTime) {
				clicks.poll();
			}
			return clicks.size();
		}
	}

	public void updateClicks(Player player) {
		ArrayDeque<Long> clicks = playerClicks.computeIfAbsent(player.getUniqueId(), id -> new ArrayDeque<>());

		synchronized (clicks) {
			clicks.add(System.currentTimeMillis());
		}
	}

	public void showCPS(Player player) {
		showCps.add(player);
		getCPSBottomLine().updatePlayer(player, getCPSText(player));
	}

	public void hideCPS(Player player) {
		showCps.remove(player);
		getCPSBottomLine().removePlayer(player);
	}

	public boolean isShowingCPS(Player player) {
		return showCps.contains(player);
	}

	private BottomLine cpsBottomLine;

	public BottomLine getCPSBottomLine() {
		if (cpsBottomLine == null) {
			cpsBottomLine = BottomLineAPI.createBottomLine("cps", 0);
			cpsBottomLine.updatePeriodically((player, oldText) -> {
				if (!isShowingCPS(player)) {
					return null;
				}

				return getCPSText(player);
			}, 1L);
		}
		return cpsBottomLine;
	}

	public String getCPSText(Player p) {
		return ChatColor.GOLD + "" + ChatColor.BOLD + "CPS: " + ChatColor.YELLOW + Finale.getPlugin().getManager().getCPSHandler().getCPS(p.getUniqueId());
	}
}
