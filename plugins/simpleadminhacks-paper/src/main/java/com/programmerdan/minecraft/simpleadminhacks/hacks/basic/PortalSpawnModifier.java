package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import java.util.Iterator;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;

public class PortalSpawnModifier extends BasicHack {
	
	private Random rng;
	
	@AutoLoad
	private double pigManChance;
	@AutoLoad
	private double ghastChance;
	@AutoLoad
	private double witherSkeletonChance;
	
	@AutoLoad
	private double witherHeadDropChance;

	public PortalSpawnModifier(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		rng = new Random();
	}
	
	@EventHandler
	public void portalSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() != SpawnReason.NETHER_PORTAL) {
			return;
		}
		if (event.getEntityType() == EntityType.WITHER_SKELETON) {
			event.setCancelled(roll(witherSkeletonChance));
			return;
		}
		if (event.getEntityType() == EntityType.PIG_ZOMBIE) {
			if (!roll(pigManChance)) {
				event.setCancelled(true);
				return;
			}
			if (roll(ghastChance)) {
				event.setCancelled(true);
				spawnGhast(event.getLocation());
			}
		}
	}
	
	private void spawnGhast(Location loc) {
		//TODO check for space?
		loc.getWorld().spawnEntity(loc.add(0, 2, 0), EntityType.GHAST);
	}
	
	private boolean roll(double chance) {
		return rng.nextDouble() <= chance;
	}
	
	@EventHandler
	public void witherSkeleDeath(EntityDeathEvent e) {
		if (e.getEntityType() != EntityType.WITHER_SKELETON) {
			return;
		}
		Iterator<ItemStack> iter = e.getDrops().iterator();
		while(iter.hasNext()) {
			ItemStack is = iter.next();
			if (is.getType() == Material.WITHER_SKELETON_SKULL) {
				iter.remove();
			}
		}
		if (roll(witherHeadDropChance)) {
			e.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
		}
	}
	


}
