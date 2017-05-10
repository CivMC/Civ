package com.github.devotedmc.hiddenore.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;

/**
 * Populator to strip out blocks selectively from a world during generation. 
 * 
 * @author ProgrammerDan
 */
public class WorldGenerationListener implements Listener {

	Set<Material> toReplace = null;
	Material replaceWith = null;
	String worldName = null;
	
	/**
	 * When creating, pass in a config with three sub-elements
	 * <br/>
	 * <code>
	 *   world: world_name
	 *   replace:
	 *   - IRON_ORE
	 *   - REDSTONE_ORE
	 *   with: STONE
	 * </code>
	 * <br/>
	 * This should be specified per world.
	 * 
	 * @param config The world-specific config.
	 * 
	 */
	public WorldGenerationListener(ConfigurationSection config) {
		if (config.contains("world")) {
			worldName = config.getString("world");
		}
		if (config.contains("replace")) {
			toReplace = new HashSet<Material>();
			for (String replace : config.getStringList("replace")) {
				Material rMat = Material.matchMaterial(replace);
				if (rMat != null) {
					toReplace.add(rMat);
				}
			}
		}
		if (config.contains("with")) {
			String with = config.getString("with");
			replaceWith = Material.matchMaterial(with);
		}
	}
	
	/**
	 * Reviews the chunk line by line and replaces all instances of toReplace with replaceWith.
	 * This is configured world to world.
	 * 
	 * @param event ChunkPopulateEvent covering the chunk 
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void postGenerationOreClear(ChunkPopulateEvent event) {
		if (toReplace == null || replaceWith == null || worldName == null) {
			return;
		}
		
		Chunk chunk = event.getChunk();
		
		if (!chunk.getWorld().getName().equalsIgnoreCase(worldName)) {
			return;
		}
		
		// now scan the chunk for ores and remove them.
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					Block block = chunk.getBlock(x, y, z);
					Material mat = block.getType();
					
					if (toReplace.contains(mat)) {
						block.setType(mat, true);
					}
				}
			}
		}
	}
}
