package com.untamedears.realisticbiomes.listener;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.GrowthConfig;

/**
 * Event listener for all plant growth related events. Whenever a crop, plant block, or sapling attempts to grow, its type
 * is checked against the biomes in which it is permitted to grow. If the biome is not permitted, the event is canceled and
 * the plant does not grow. Additionally, all instances of bonemeal being used as fertilizer are canceled.
 * @author WildWeazel
 *
 */
public class GrowListener implements Listener {

	private HashMap<Object, GrowthConfig> growthMap;
	
	public GrowListener(HashMap<Object, GrowthConfig> growthMap) {
		super();
		
		this.growthMap = growthMap;
	}

	/**
	 *  Event handler for {@link BlockGrowEvent}. Checks plant growth for proper conditions
	 * @param event The {@link BlockGrowEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void growBlock(BlockGrowEvent event) {
		Material m = event.getNewState().getType();
		Block b = event.getBlock();
		event.setCancelled(!willGrow(m, b));
	}

	/**
	 * Event handler for {@link StructureGrowEvent}. Checks tree growth for proper conditions
	 * @param event The {@link StructureGrowEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void growStructure(StructureGrowEvent event) {
		TreeType t = event.getSpecies();
		Block b = event.getLocation().getBlock();
		event.setCancelled(!willGrow(t, b));
	}

	/**
	 * Event handler for {@link PlayerInteractEvent}. Cancels all uses of Bonemeal as an item.
	 * @param event The {@link PlayerInteractEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	
	public void onPlayerInteract(PlayerInteractEvent event) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        ItemStack item = event.getPlayer().getItemInHand();
                        // Ink Sack with data 15  == Bone Meal
                        if (item.getTypeId() == 351 && item.getData().getData() == 15) {
                                        event.setCancelled(true);
                        }
            }
        }
	

	/**
	 * Determines if a plant {@link Material | @link TreeType} will grow, given the current conditions
	 * @param m The material type of the plant
	 * @param b The block that the plant is on
	 * @return Whether the plant will grow this tick
	 */
	private boolean willGrow(Object m, Block b) {
		if(growthMap.containsKey(m)) {
			return Math.random() < growthMap.get(m).getRate(b);
		}
		return true;
	}
}
