package com.untamedears.realisticbiomes.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.inventory.items.TreeTypeUtils;

import java.util.ArrayList;
import java.util.List;

public class MobListener implements Listener
{
	private final static List<Material> crops = new ArrayList<>();

	public MobListener()
	{
		crops.add(Material.POTATOES);
		crops.add(Material.BEETROOTS);
		crops.add(Material.CARROTS);
		crops.add(Material.WHEAT);
	}

	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (e.getEntityType() == EntityType.VILLAGER) {
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
	}
}
