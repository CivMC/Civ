package com.untamedears.realisticbiomes.persist;

import java.util.UUID;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;

public class WorldID {
	private static UUID overworldID;
	private static UUID netherID;
	private static UUID endID;
	
	public static void init (Plugin plugin) {
		World overworld = plugin.getServer().getWorld("world");
		WorldID.overworldID = overworld == null ? null : overworld.getUID();
		
		World nether = plugin.getServer().getWorld("world_nether");
		WorldID.netherID = nether == null ? null : nether.getUID();
		
		World end = plugin.getServer().getWorld("world_the_end");
		WorldID.endID = end == null ? null : end.getUID();
	}
	
	public static UUID getMCID(int id) {
		if (id == 0)
			return overworldID;
		else if (id == 1)
			return netherID;
		else if (id == 2)
			return endID;
		else
			return null;
	}
	
	public static int getPID(UUID id) {
		if (id == overworldID)
			return 0;
		else if (id == netherID)
			return 1;
		else if (id == endID)
			return 2;
		else
			return -1;		
	}
}
