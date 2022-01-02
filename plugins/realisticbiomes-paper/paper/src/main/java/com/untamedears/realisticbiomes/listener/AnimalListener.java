package com.untamedears.realisticbiomes.listener;

import com.untamedears.realisticbiomes.AnimalConfigManager;
import com.untamedears.realisticbiomes.growthconfig.AnimalMateConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ItemSpawnEvent;

public class AnimalListener implements Listener {

	private final AnimalConfigManager animalManager;

	public AnimalListener(AnimalConfigManager animalManager) {
		this.animalManager = animalManager;
	}

	@EventHandler
	public void spawnEntity(CreatureSpawnEvent event) {
		if (event.getSpawnReason() == SpawnReason.BREEDING) {
			EntityType type = event.getEntityType();
			Block block = event.getLocation().getBlock();

			if (!willSpawn(type, block)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void spawnItem(ItemSpawnEvent event) {
		if (event.getEntity() instanceof Player) {
			return;
		}
		if (event.getEntity().getItemStack().getType() == Material.EGG) {
			if (!willSpawn(EntityType.CHICKEN, event.getLocation().getBlock())) {
				event.setCancelled(true);
			}
		}
	}

	private boolean willSpawn(EntityType type, Block b) {
		AnimalMateConfig config = animalManager.getAnimalMateConfig(type);
		if (config == null) {
			// vanilla
			return true;
		}
		return Math.random() < config.getRate(b.getBiome());
	}
}
