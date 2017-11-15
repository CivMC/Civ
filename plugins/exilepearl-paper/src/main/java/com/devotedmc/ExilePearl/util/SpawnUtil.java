package com.devotedmc.ExilePearl.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.josvth.randomspawn.RandomSpawn;

import com.devotedmc.ExilePearl.ExilePearlPlugin;

public class SpawnUtil {
	
	/**
	 * Spawns a player in the overworld without killing them
	 * Uses Randomspawn if available, otherwise natural spawn
	 * @param player The player to spawn
	 */
	public static void spawnPlayer(Player player) {
		if(ExilePearlPlugin.getApi().isRandomSpawnEnabled()) {
			RandomSpawn randomSpawn = (RandomSpawn) Bukkit.getPluginManager().getPlugin("RandomSpawn");
			player.teleport(randomSpawn.chooseSpawn(Bukkit.getWorlds().get(0)));
		} else {
			player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
		}
	}
}
