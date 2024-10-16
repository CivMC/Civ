package com.github.maxopoly.finale.combat;

import com.github.maxopoly.finale.Finale;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class SprintHandler implements Listener {

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();

		if (sprinting.contains(player.getUniqueId())) {
			sprinting.remove(player.getUniqueId());
		}
	}

	private Set<UUID> sprinting = new HashSet<>();

	public SprintHandler() {
		Bukkit.getPluginManager().registerEvents(this, Finale.getPlugin());
	}

	public void startSprinting(net.minecraft.world.entity.player.Player player) {
		startSprinting(player.getUUID());
	}

	public void stopSprinting(net.minecraft.world.entity.player.Player player) {
		stopSprinting(player.getUUID());
	}

	public void startSprinting(Player player) {
		startSprinting(player.getUniqueId());
	}

	public void stopSprinting(Player player) {
		stopSprinting(player.getUniqueId());
	}

	public void startSprinting(UUID uuid) {
		sprinting.add(uuid);
	}

	public void stopSprinting(UUID uuid) {
		sprinting.remove(uuid);
	}

	public boolean isSprinting(net.minecraft.world.entity.player.Player player) {
		return isSprinting(player.getUUID());
	}

	public boolean isSprinting(Player player) {
		return isSprinting(player.getUniqueId());
	}

	public boolean isSprinting(UUID uuid) {
		return sprinting.contains(uuid);
	}


}
