package com.untamedears.realisticbiomes.persist;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import com.untamedears.realisticbiomes.RealisticBiomes;

public class WorldID {
	/**
	 * There shouldn't be a distinction between the three default worlds
	 * and additional or other worlds with other names, but this change
	 * would require a DB schema update. This would either have to be handled
	 * in code during plugin initialization, or manually with a detection and
	 * descriptive warning when used on a non-updated DB. 
	 */
	private static UUID overworldID;
	private static UUID netherID;
	private static UUID endID;
	private static HashMap<UUID, Integer> otherWorlds = null;
	
	public static void init (Plugin plugin) {
		World overworld = plugin.getServer().getWorld("world");
		WorldID.overworldID = overworld == null ? null : overworld.getUID();
		
		World nether = plugin.getServer().getWorld("world_nether");
		WorldID.netherID = nether == null ? null : nether.getUID();
		
		World end = plugin.getServer().getWorld("world_the_end");
		WorldID.endID = end == null ? null : end.getUID();
		
		for (World world: plugin.getServer().getWorlds()) {
			if (!world.getName().equals("world")
					&& !world.getName().equals("world_nether")
					&& !world.getName().equals("world_the_end")) {
				if (otherWorlds == null) {
					RealisticBiomes.doLog(Level.WARNING, "There are worlds that don't have default names (world, world_nether, world_the_end), experimental feature");
					otherWorlds = new HashMap<UUID, Integer>();
				}
				otherWorlds.put(world.getUID(), world.getUID().hashCode());
			}
		}
	}
	
	/**
	 * @param id DB world id
	 * @return UUID or null if id not found
	 */
	public static UUID getMCID(int id) {
		if (id == 0) {
			return overworldID;
		} else if (id == 1) {
			return netherID;
		} else if (id == 2) {
			return endID;
		} else {
			if (otherWorlds != null) {
				for (Map.Entry<UUID, Integer> entry: otherWorlds.entrySet()) {
					if (entry.getValue() == id) {
						return entry.getKey();
					}
				}
			}
			RealisticBiomes.doLog(Level.SEVERE, "Cannot get UUID for world with id: " + id);
			return null;
		}
	}
	
	/**
	 * @param id World UUID
	 * @return DB id or -1 if not found
	 */
	public static int getPID(UUID id) {
		if (id.equals(overworldID)) {
			return 0;
		} else if (id.equals(netherID)) {
			return 1;
		} else if (id.equals(endID)) {
			return 2;
		} else if (otherWorlds != null && otherWorlds.containsKey(id)) {
			return otherWorlds.get(id);
		} else {
			RealisticBiomes.doLog(Level.SEVERE, "Cannot get world id for UUID: " + id);
			return -1;
		}
	}
}
