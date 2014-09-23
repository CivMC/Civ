package com.untamedears.realisticbiomes.listener;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.material.MaterialData;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.persist.ChunkCoords;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.realisticbiomes.persist.WorldID;

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
		Material m = event.getNewState().getType();
		Block b = event.getBlock();
		
		GrowthConfig growthConfig = plugin.getGrowthConfig(b);
		if (plugin.persistConfig.enabled && growthConfig != null && growthConfig.isPersistent()) {
			plugin.growAndPersistBlock(b, true);
			
			event.setCancelled(true);
		}
		else if (!willGrow(m, b)) {
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
		
		TreeType t = event.getSpecies();
		
		Block b = event.getLocation().getBlock();
		
		if (!willGrow(t, b)) {
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
            // Ink Sack with data 15  == Bone Meal
            if (item.getTypeId() == 351 && item.getData().getData() == 15) {
            	Material material = event.getClickedBlock().getType();
    			if (material != Material.SAPLING && plugin.hasGrowthConfig(event.getClickedBlock())) {
        			event.setCancelled(true);
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
	        	if(event.getBlock().getRelative(face).getType() == Material.CROPS) {
	        		event.setCancelled(true);
	        	}
	        	if(event.getBlock().getRelative(face).getType() == Material.SOIL) {
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
		GrowthConfig config = plugin.getGrowthConfig(m);
		
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
		if(plugin.hasGrowthConfig(m)) {
			boolean willGrow = Math.random() < plugin.getGrowthConfig(m).getRate(b);
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
		plugin.getPlantManager().growChunk(e.getChunk());		
	}
	
	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		if (!plugin.persistConfig.enabled)
			return;
		
		ChunkCoords coords = new ChunkCoords(e.getChunk());
		plugin.getPlantManager().minecraftChunkUnloaded(coords);
		
		// TESTING
		//this.plugin.getLogger().info("ChunkUnLoaded: " + coords);

	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (!plugin.persistConfig.enabled)
			return;
		
		// if the block placed was a recognized crop, register it with the manager
		Block block = event.getBlockPlaced();
		GrowthConfig growthConfig = plugin.getGrowthConfig(block);
		if (growthConfig == null)
			return;	
		
		plugin.getPlantManager().addPlant(block, new Plant(System.currentTimeMillis() / 1000L));
	}
	
	@EventHandler
	public void onWorldLoadEvent(WorldInitEvent e) {
		WorldID.init(plugin);

	}
}
