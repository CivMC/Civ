package com.untamedears.realisticbiomes.listener;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.BaseConfig;
import com.untamedears.realisticbiomes.GrowthConfig;

/**
 * Event listeners for animal spawn related events. Whenever animals breed or a fish is caught, the species is checked against
 * allowed biomes. If the biome is not found, the event is cancelled and nothing happens. Also, chicken egg spawns are cancelled
 * randomly to reduce the average time between laying eggs.
 * @author WildWeazel
 *
 */
public class SpawnListener implements Listener {

	private final HashMap<Object, GrowthConfig> growthMap;
	private final HashMap<Object, BaseConfig> fishMap;
	
	public SpawnListener(HashMap<Object, GrowthConfig> growthMap, HashMap<Object, BaseConfig> fishMap) {
		super();
		
		this.growthMap = growthMap;
		this.fishMap = fishMap;
	}

	/**
	 *  Event handler for {@link CreatureSpawnEvent}. Checks animal breeding for proper biomes.
	 * @param event The {@link CreatureSpawnEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void spawnEntity(CreatureSpawnEvent event) {
		if(event.getSpawnReason() == SpawnReason.BREEDING) {
			EntityType type = event.getEntityType();
			Block block = event.getLocation().getBlock();
			
			if (!willSpawn(type, block)) {
				event.setCancelled(true);
			}
		}
	}

	
	/**
	 *  Event handler for {@link ItemSpawnEvent}. Reduces the chance of a chicken egg being spawned.
	 * @param event The {@link ItemSpawnEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void spawnItem(ItemSpawnEvent event) {
		if (event.getEntity() instanceof Player){
			return;
		}
		if(event.getEntity().getItemStack().getType() == Material.EGG) {
			Block b = event.getLocation().getBlock();
			Material m = Material.EGG;
			
			if (!willSpawn(m , b)) {
				event.setCancelled(true);
			}
		}
	}

	/**
	 *  Event handler for {@link PlayerFishEvent}. Checks caught fish for proper biomes.
	 * @param event The {@link PlayerFishEvent} being handled
	 */
	@EventHandler(ignoreCancelled = true)
	public void fishing(PlayerFishEvent event) {
		if(event.getState() == PlayerFishEvent.State.CAUGHT_FISH && event.getCaught() != null && event.getCaught() instanceof Item) {
			ItemStack items = ((Item)event.getCaught()).getItemStack();
			Material type = items.getType();
			Block block = event.getCaught().getLocation().getBlock();
			
			if (!fishWillSpawn(type, block)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("Fish got away");
			}
		}
	}

	/**
	 * Determines if an entity {@link EntityTypw | @link Material} will spawn, given the current conditions
	 * @param m The material type of the
	 * @param b The block that the plant is on
	 * @return Whether the plant will grow this tick
	 */
	private boolean willSpawn(Object e, Block b) {
		if(growthMap.containsKey(e)) {
			return Math.random() < growthMap.get(e).getRate(b);
		}
		return true;
	}

	/**
	 * Determines if an ItemStack will spawn from fishing, given the current conditions
	 * @param m The material type of the
	 * @param b The block that the plant is on
	 * @return Whether the item will spawn
	 */
	private boolean fishWillSpawn(Object e, Block b) {
		if(fishMap.containsKey(e)) {
			return Math.random() < fishMap.get(e).getRate(b);
		}
		return true;
	}

}
