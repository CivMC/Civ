package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
		
		if (playerClicks.containsKey(player.getUniqueId())) {
			playerClicks.remove(player.getUniqueId());
		}
		
		if (showCps.contains(player)) {
			showCps.remove(player);
		}
	}

	private Map<UUID, List<Long>> playerClicks = new ConcurrentHashMap<>();
	private Set<Player> showCps = Sets.newHashSet();

	public CPSHandler() {
		Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
	}
	
	public int getCPS(UUID uuid) {
		 final long time = System.currentTimeMillis();
		 List<Long> clicks = this.playerClicks.get(uuid);
		 if (clicks == null) {
		 	return 0;
		 }
	     Iterator<Long> iterator = clicks.iterator();
	     while (iterator.hasNext()) {
	    	 if (iterator.next() + Finale.getPlugin().getManager().getCombatConfig().getCpsCounterInterval() < time) {
	    		 iterator.remove();
	         }
	     }
	     return this.playerClicks.get(uuid).size();
	}
 
	public void updateClicks(Player player) {
	 	List<Long> clicks = playerClicks.get(player.getUniqueId());
	 	if (clicks == null) {
		 	clicks = new ArrayList<>();
		 	playerClicks.put(player.getUniqueId(), clicks);
	 	}
	 	clicks.add(System.currentTimeMillis());
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
