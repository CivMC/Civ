package com.github.devotedmc.hiddenore.listeners;

import com.github.devotedmc.hiddenore.BlockConfig;
import com.github.devotedmc.hiddenore.Config;
import com.github.devotedmc.hiddenore.HiddenOre;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkPopulateEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Populator to strip out blocks selectively from a world during generation. 
 * 
 * @author ProgrammerDan
 */
public class WorldGenerationListener implements Listener {

	Map<Material, Material> replacements = null;
	String worldName = null;
	UUID worldUUID = null;
	
	/**
	 * When creating, pass in a config with three sub-elements. Now supports UUID reference of world.
	 * <br>
	 * <code>
	 *   world: world_name (or UUID)
	 *   replace:
	 *     IRON_ORE: STONE
	 *     REDSTONE_ORE: STONE
	 * </code>
	 * <br>
	 * This should be specified per world.
	 * 
	 * @param config The world-specific config.
	 * 
	 */
	public WorldGenerationListener(ConfigurationSection config) {
		if (config.contains("world")) {
			worldName = config.getString("world");
		}
		try {
			if (worldName != null) {
				World world = HiddenOre.getPlugin().getServer().getWorld(worldName);
				if (world != null) {
					worldUUID = world.getUID();
				} else {
					worldUUID = UUID.fromString(worldName);
				}
			}
		} catch (IllegalArgumentException iae) {
			worldUUID = null;
		}
		if (config.contains("replace")) {
			replacements = new HashMap<>();
			for (String replace : config.getConfigurationSection("replace").getKeys(false)) {
				Material rMat = Material.matchMaterial(replace.toUpperCase());
				System.out.println(config.getCurrentPath());
				Material wMat = Material.matchMaterial(config.getConfigurationSection("replace").getString(replace));
				if (rMat != null && wMat != null) {
					replacements.put(rMat, wMat);
				}
			}
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
		if (replacements == null || (worldName == null && worldUUID == null) ) {
			return;
		}
		
		Chunk chunk = event.getChunk();
		
		World world = chunk.getWorld();
		
		if (!world.getName().equalsIgnoreCase(worldName) && !world.getUID().equals(worldUUID)) {
			return;
		}
		
		clear(chunk);
		
		int x = chunk.getX();
		int z = chunk.getZ();
		
		// check adjacent chunks, which by contract
		// might have been updated.
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
		if(Config.caveOres) {
			generateCaveOres(chunk);
		}
	}

	private void clear(Chunk chunk) {
		int rep = 0;
		try {
			int maxY = chunk.getWorld().getMaxHeight();
			// now scan the chunk for ores and remove them.
			for (int y = chunk.getWorld().getMinHeight() + 1; y < maxY; y++) {
				for (int x = 0; x < 16; x++) {
					for (int z = 0; z < 16; z++) {
						Block block = chunk.getBlock(x, y, z);
						Material mat = block.getType();
						
						if (replacements.containsKey(mat)) {
							rep++;
							block.setType(replacements.get(mat), false);
						}
					}
				}
			}
			if (maxY < 32) {
				HiddenOre.getPlugin().getLogger().log(Level.WARNING, "Chunk height abnormally low: {0} at {1}, {2}",
						new Object[]{maxY, chunk.getX(), chunk.getZ()});
			}
		} catch (Exception e) {
			HiddenOre.getPlugin().getLogger().log(Level.SEVERE, "Failed to clear ores from chunk at {0}, {1} with error {2}",
					new Object[]{chunk.getX(), chunk.getZ(), e.getMessage()});
		}
		
		if (rep > 0 && Config.isDebug) {
			HiddenOre.getPlugin().getLogger().log(Level.INFO, "Replaced {0} blocks at {1}, {2}", new Object[]{rep, chunk.getX(), chunk.getZ()});
		}
	}

	static BlockFace[] faces = new BlockFace[] {BlockFace.UP,BlockFace.DOWN,BlockFace.NORTH,BlockFace.SOUTH,BlockFace.EAST,BlockFace.WEST};
	private void generateCaveOres(Chunk chunk) {
		UUID world = chunk.getWorld().getUID();
		int xzmax = chunk.getWorld().getMaxHeight();
		ItemStack breakItem = new ItemStack(Material.DIAMOND_PICKAXE);
		for(int x = 0; x < 16; x++) {
			for(int z = 0; z < 16; z++) {
				for(int y = chunk.getWorld().getMinHeight(); y < xzmax; y++) {
					Block block = chunk.getBlock(x, y, z);
					BlockConfig bc = Config.isDropBlock(world, block.getBlockData());
					if(bc == null) continue;
					for(BlockFace face : faces) {
						if(block.getRelative(face).getType() == Material.AIR) {
							BlockBreakListener.spoofBlockBreak(block.getLocation(), block, breakItem);
							break;
						}
					}
				}
			}
		}
	}
}
