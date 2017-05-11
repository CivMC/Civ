package com.github.devotedmc.hiddenore.listeners;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.generator.BlockPopulator;

import com.github.devotedmc.hiddenore.HiddenOre;

/**
 * Populator to strip out blocks selectively from a world during generation. 
 * 
 * @author ProgrammerDan
 */
public class WorldGenerationListener extends BlockPopulator implements Listener {

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
	 * Note that by contract, this is called for a single chunk but the generation can occur
	 * for surrounding chunks, if they are not yet populated.
	 * 
	 * @param event ChunkPopulateEvent covering the chunk 
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void postGenerationOreClear(ChunkPopulateEvent event) {
		if (toReplace == null || replaceWith == null || worldName == null) {
			return;
		}
		
		Chunk chunk = event.getChunk();
		
		World world = chunk.getWorld();
		
		if (!world.getName().equalsIgnoreCase(worldName)) {
			return;
		}
		
		clear(chunk);
		
		int x = chunk.getX();
		int z = chunk.getZ();
		
		// check adjacent chunks.
		if (world.isChunkLoaded(x - 1, z) ) {
			chunk = world.getChunkAt(x - 1, z);
			clear(chunk);
		}
		
		if (world.isChunkLoaded(x + 1, z) ) {
			chunk = world.getChunkAt(x + 1, z);
			clear(chunk);
		}

		if (world.isChunkLoaded(x, z - 1) ) {
			chunk = world.getChunkAt(x, z - 1);
			clear(chunk);
		}

		if (world.isChunkLoaded(x, z + 1) ) {
			chunk = world.getChunkAt(x, z + 1);
			clear(chunk);
		}
	}

	@Override
	public void populate(World world, Random random, Chunk source) {
		if (toReplace == null || replaceWith == null || worldName == null) {
			return;
		}
		
		if (!world.getName().equalsIgnoreCase(worldName)) {
			return;
		}
		
		clear(source);
	}
	
	private void clear(Chunk chunk) {
		int rep = 0;
		
		// now scan the chunk for ores and remove them.
		for (int y = 1; y < chunk.getWorld().getMaxHeight(); y++) {
			for (int x = 0; x < 16; x++) {
				for (int z = 0; z < 16; z++) {
					Block block = chunk.getBlock(x, y, z);
					Material mat = block.getType();
					
					if (toReplace.contains(mat)) {
						rep++;
						block.setType(replaceWith, true);
						/*BlockState bs = block.getState();
						bs.setType(mat);
						bs.update(true, true);*/
					}
				}
			}
		}
		
		if (rep > 0) {
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "Replaced {0} blocks at {1}, {2}", new Object[]{rep, chunk.getX(), chunk.getZ()});
		}
	}
}
