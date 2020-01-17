package vg.civcraft.mc.civmodcore.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DoubleInteractFixer {

	private Map<UUID, List<Location>> locations;

	public DoubleInteractFixer(Plugin plugin) {
		locations = new TreeMap<>();
		Bukkit.getScheduler().runTaskTimer(plugin, () -> locations.clear(), 1L, 1L);
	}

	/**
	 * Checks if the given player has already interacted with the given block this
	 * tick and counts such an interact for this tick if one doesnt already exist.
	 * This method will only return true once for the same player and block during
	 * the same tick and then never again. All tracking is reset every finished tick
	 * 
	 * @param player Player who interacted with the block
	 * @param block  Block the player interacted with
	 * @return True if the player already interacted, false if not
	 */
	public boolean checkInteracted(Player player, Block block) {
		if (player == null) {
			throw new IllegalArgumentException("Player can not be null");
		}
		if (block == null) {
			throw new IllegalArgumentException("Block can not be null");
		}
		List<Location> existingOnes = locations.computeIfAbsent(player.getUniqueId(), u -> new LinkedList<>());
		if (existingOnes.contains(block.getLocation())) {
			return true;
		}
		existingOnes.add(block.getLocation());
		return false;
	}

}
