package com.untamedears.realisticbiomes.listener;

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

import com.untamedears.realisticbiomes.GrowthMap;

/**
 * Event listeners for animal spawn related events. Whenever animals breed or a fish is caught, the species is checked against
 * allowed biomes. If the biome is not found, the event is cancelled and nothing happens. Also, chicken egg spawns are cancelled
 * randomly to reduce the average time between laying eggs.
 * @author WildWeazel
 *
 */
public class SpawnListener implements Listener {

	private final GrowthMap growthMap;
	
	public SpawnListener(GrowthMap growthMap) {
		super();
		
		this.growthMap = growthMap;
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
			// short fishType = items.getDurability(); // fish type has no MaterialData subclass... need to change GrowthMap to key this
			Block block = event.getCaught().getLocation().getBlock();
			
			if (!fishWillSpawn(type, block)) {
				event.setCancelled(true);
				event.getPlayer().sendMessage("Fish got away");
			}
		}
	}

	/**
	 * Determines if an entity {@link Material} will spawn, given the current conditions
	 * @param e The entity type
	 * @param b The block that the plant is on
	 * @return Whether the plant will grow this tick
	 */
	private boolean willSpawn(Material m, Block b) {
		if(growthMap.containsKey(m)) {
			return Math.random() < growthMap.get(m).getRate(b);
		}
		return true;
	}
	
	/**
	 * Determines if a material {@link EntityType} will spawn an entity, given the current conditions
	 * @param m The material type of the
	 * @param b The block that the plant is on
	 * @return Whether the plant will grow this tick
	 */
	private boolean willSpawn(EntityType e, Block b) {
		if(growthMap.containsKey(e)) {
			return Math.random() < growthMap.get(e).getRate(b);
		}
		return true;
	}

	/**
	 * Determines if an ItemStack will spawn from fishing, given the current conditions
	 * @param fishType 
	 * @param m The material type of the
	 * @param b The block that the plant is on
	 * @return Whether the item will spawn
	 */
	private boolean fishWillSpawn(Material e, Block b) {
		if(growthMap.containsKey(e)) {
			return Math.random() < growthMap.get(e).getRate(b);
		}
		return true;
	}

}
