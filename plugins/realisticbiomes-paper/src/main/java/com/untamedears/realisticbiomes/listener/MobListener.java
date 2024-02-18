package com.untamedears.realisticbiomes.listener;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class MobListener implements Listener
{
	private final static List<Material> crops = new ArrayList<>();

	public MobListener() {
		crops.add(Material.POTATOES);
		crops.add(Material.BEETROOTS);
		crops.add(Material.CARROTS);
		crops.add(Material.WHEAT);
		crops.add(Material.SWEET_BERRIES);
		crops.add(Material.SWEET_BERRY_BUSH);
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (e.getEntityType() == EntityType.VILLAGER) {
			if (crops.contains(e.getBlock().getType()) || crops.contains(e.getTo())) {
				e.setCancelled(true);
			}
		}
		if (e.getEntityType() == EntityType.FOX) {
			if (crops.contains(e.getBlock().getType()) || crops.contains(e.getTo())) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent e) {
		if (e.getEntityType() == EntityType.VILLAGER) {
			e.setCancelled(true);
		}
		if (e.getEntityType() == EntityType.FOX) {
			e.setCancelled(true);
		}
	}
}
