package com.untamedears.realisticbiomes.listener;


import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.persist.ChunkCoords;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.realisticbiomes.persist.WorldID;
import com.untamedears.realisticbiomes.utils.Fruits;
import com.untamedears.realisticbiomes.utils.MaterialAliases;
import com.untamedears.realisticbiomes.utils.Trees;

/**
 * Event listener for all plant growth related events. Whenever a crop, plant block, or sapling attempts to grow, its type
 * is checked against the biomes in which it is permitted to grow. If the biome is not permitted, the event is canceled and
 * the plant does not grow. Additionally, all instances of bonemeal being used as fertilizer are canceled.
 * @author WildWeazel
 *
 */
public class GrowListener implements Listener {
	
	private final RealisticBiomes plugin;
	
	public GrowListener(RealisticBiomes plugin) {
		super();
		
		this.plugin = plugin;
	}

	/**
	 *  Event handler for {@link BlockGrowEvent}. Checks plant growth for proper conditions
	 * @param event The {@link BlockGrowEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockGrow(BlockGrowEvent event) {
		Material material = event.getNewState().getType();
		Block block = event.getBlock();
		
		if (growFruit(block, material, false)) {
			event.setCancelled(true);
			return;
		}
		
		GrowthConfig growthConfig = plugin.materialGrowth.get(material);
		if (plugin.persistConfig.enabled && growthConfig != null && growthConfig.isPersistent()) {
			event.setCancelled(true);
			
			if (MaterialAliases.isColumnBlock(material)) {
				block = block.getRelative(BlockFace.DOWN);
			}
			block = MaterialAliases.getOriginBlock(block, material);
			if (block != null) {
				plugin.growAndPersistBlock(block, true, growthConfig, null, null);
			}

		} else if (!willGrow(material, block)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Event handler for {@link StructureGrowEvent}. Checks tree growth for proper conditions
	 * @param event The {@link StructureGrowEvent} being handled
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onStructureGrow(StructureGrowEvent event) {
		// disable bonemeal
		if (event.isFromBonemeal()) {
			event.setCancelled(true);
			return;
		}
		
		TreeType type = event.getSpecies();
		if (type == TreeType.BIG_TREE) {
			event.setCancelled(true);
			return;
		}
		
		Block block = event.getLocation().getBlock();
		
		GrowthConfig growthConfig = plugin.materialGrowth.get(type);
		if (plugin.persistConfig.enabled && growthConfig != null && growthConfig.isPersistent()) {
			growTree(block, type, growthConfig);
			event.setCancelled(true);
			
		} else if (!willGrow(type, block)) {
			event.setCancelled(true);
		}
	}

	/**
	 * Event handler for {@link PlayerInteractEvent}. Cancels all uses of Bonemeal as an item on crops registered in the config.
	 * @param event The {@link PlayerInteractEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)

	public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getPlayer().getItemInHand();
            // Ink Sack with white dye is Bone Meal
            if (item.getType() == Material.INK_SACK) {
            	MaterialData data = item.getData();
            	if (data instanceof Dye && ((Dye)data).getColor() == DyeColor.WHITE) {
            		Material material = event.getClickedBlock().getType();
        			if (material != Material.SAPLING
        					&& MaterialAliases.getConfig(plugin.materialGrowth, event.getClickedBlock()) != null) {
            			event.setCancelled(true);
        			}
        			else if(!plugin.allowTallPlantReplication && material == Material.DOUBLE_PLANT) {
        				event.setCancelled(true);
        			}
            	}
            }
        }
    }
	
	/**
	 * Event handler for {@link BlockDispenseEvent}. Cancels all uses of Bonemeal used by a dispenser on crops registered in the config.
	 * @param event The {@link PlayerDispenseEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)

	public void onBlockDispense(BlockDispenseEvent event) {		
		// here we check to make sure there is a item associated with the event
		// to make sure the item is a InkSack (the 'root' item for all dyes)
		// and then make sure that the item is Bonemeal (different color dyes have
		// different data values)
		
		/*
		// Debugging: 
		RealisticBiomes.LOG.warning("onBlockDispense called: event.getItem() is " + event.getItem());
		RealisticBiomes.LOG.warning("\titem type: " + event.getItem().getType());
		RealisticBiomes.LOG.warning("\tdataval: " + event.getItem().getData().getData());
		*/
		
		
		// NOTE NOTE NOTE
		// Apparently there is a bug in spigot where in dispensers, a dispensed item that is of type INK_SACK
		// , if its bonemeal, then it says the 'data' value of that item is 0, which is the regular INK_SACK rather then
		// bonemeal, it seems to only affect bonemeal, as all the other dye types seem to have the correct behavior
		// so for now, i'm just checking for INK_SACK, cause all this does is just prevent the dispense, and if
		// users can't dispense other types of dye, oh noooo
		//
		// also see: http://www.spigotmc.org/threads/blockdispenseevent-does-not-provide-item-durability.3444/
		//
		if (event.getItem() != null 
				&& event.getItem().getType() == Material.INK_SACK) {// if its a ink_sack we know that it has a MaterialData and that has 'data' for type of dye
			
	        if (event.getBlock().getType() == Material.DISPENSER) {
	        	MaterialData d = event.getBlock().getState().getData();
	        	Dispenser disp = (Dispenser) d;
	        	BlockFace face = disp.getFacing();
	        	Material mat = event.getBlock().getRelative(face).getType();
	        	if (mat == Material.CROPS || mat == Material.SOIL || mat == Material.SAPLING || mat == Material.BROWN_MUSHROOM || mat == Material.RED_MUSHROOM) {
	        		event.setCancelled(true);
	        	}
	        }
		}        
    }
	

	/**
	 * Determines if a plant {@link Material | @link TreeType} will grow, given the current conditions
	 * @param m The material type of the plant
	 * @param b The block that the plant is on
	 * @return true if the block should grow this material, otherwise false
	 */
	private boolean willGrow(Material m, Block b) {
		GrowthConfig config = plugin.materialGrowth.get(m);
		
		// Returns true if the random value is within the growth rate
		if (config != null) {
			return Math.random() < config.getRate(b);
		}
		
		// Default to growth if not cofigured
		return true;
	}
	

	/**
	 * Determines if a plant {@link Material | @link TreeType} will grow, given the current conditions
	 * @param m The material type of the plant
	 * @param b The block that the plant is on
	 * @return Whether the plant will grow this tick
	 */
	private boolean willGrow(TreeType m, Block b) {
		if(plugin.materialGrowth.containsKey(m)) {
			boolean willGrow = Math.random() < plugin.materialGrowth.get(m).getRate(b);
			return willGrow;
		}
		return true;
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		if (!plugin.persistConfig.enabled) {
			return;
		}
		
		// Force a grow operation on all the plant records for this chunk
		// growChunk() will also verify that the chunk is loaded
		if (plugin.getPlantManager() != null) {
			plugin.getPlantManager().growChunk(e.getChunk());
		}
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (!plugin.persistConfig.enabled)
			return;
		
		ChunkCoords coords = new ChunkCoords(e.getChunk());
		if (plugin.getPlantManager() != null) {
			plugin.getPlantManager().minecraftChunkUnloaded(coords);
		}
		
		// TESTING
		//this.plugin.getLogger().info("ChunkUnLoaded: " + coords);

	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!plugin.persistConfig.enabled)
			return;
		
		// if the block placed was a recognized crop, register it with the manager
		// unless it is a fruit, then it's persistence is handled by the stem
		Block block = event.getBlockPlaced();
		if (Fruits.isFruit(block.getType())) {
			growFruit(block, false);
			return;
		}
		GrowthConfig growthConfig = MaterialAliases.getConfig(plugin.materialGrowth, block);
		if (growthConfig == null) {
			return;	
		}
		
		if (growthConfig.getRate(block) > 0.0d) {
			plugin.getPlantManager().addPlant(block, new Plant(0.0f, -1.0f));
		} else {
			block.getWorld().playEffect(block.getLocation(), Effect.VILLAGER_THUNDERCLOUD, 1, 4);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (!plugin.persistConfig.enabled)
			return;
		growFruit(event.getBlock(), true);
	}
	
	@EventHandler
	public void on(BlockPistonExtendEvent event) {
		if (!plugin.persistConfig.enabled)
			return;
		for (Block block: event.getBlocks()) {
			growFruit(block, true);
		}
	}
	
	@EventHandler
	public void on(BlockPistonRetractEvent event) {
		if (!plugin.persistConfig.enabled)
			return;
		growFruit(event.getBlock(), true);
	}
	
	/**
	 * Get all touching stems and attempt to restart their fruit growth
	 */
	private boolean growFruit(Block block, boolean ignore) {
		return growFruit(block, block.getType(), ignore);
	}
	
	private boolean growFruit(Block block, Material material, boolean ignore) {
		if (!Fruits.isFruit(material)) {
			return false;
		}
		
		GrowthConfig growthConfig = plugin.materialGrowth.get(material);
		if (!growthConfig.isPersistent()) {
			return false;
		}
		
		// only ignore block if it comes from a break event 
		Block ignoreBlock = ignore ? block : null;
		
		for (Block stem: Fruits.getStems(block, material)) {
			plugin.growAndPersistBlock(stem, true, growthConfig, ignoreBlock, null);
		}
		return true;
	}

	public void growTree(Block block, TreeType type, GrowthConfig growthConfig) {
		if (!Trees.canGrowLArge(block, type)) {
			Block originBlock = Trees.getLargeTreeOrigin(block, type);
			if (originBlock != null) {
				block = originBlock;
			}
		}
		plugin.growAndPersistBlock(block, true, growthConfig, null, null);
	}

	@EventHandler
	public void onWorldLoadEvent(WorldInitEvent e) {
		WorldID.init(plugin);

	}
}
