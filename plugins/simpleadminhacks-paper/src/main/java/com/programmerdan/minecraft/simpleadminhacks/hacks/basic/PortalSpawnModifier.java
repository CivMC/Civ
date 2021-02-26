package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

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
	
	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

	public PortalSpawnModifier(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
		rng = new Random();
	}
	
	@EventHandler
	public void portalSpawn(CreatureSpawnEvent event) {
		if (event.getSpawnReason() != SpawnReason.NETHER_PORTAL) {
			return;
		}
		if (event.getEntityType() == EntityType.ZOMBIFIED_PIGLIN) {
			if (!roll(pigManChance)) {
				event.setCancelled(true);
				return;
			}
			if (roll(ghastChance)) {
				event.setCancelled(true);
				spawnGhast(event.getLocation());
			}
			else if (roll(witherSkeletonChance)) {
				event.setCancelled(true);
				spawnWitherSkeleton(event.getLocation());
			}
		}
	}
	
	private void spawnGhast(Location loc) {
		//TODO check for space?
		loc.getWorld().spawnEntity(loc.add(0, 2, 0), EntityType.GHAST);
	}
	
	private void spawnWitherSkeleton(Location loc) {
		loc.getWorld().spawnEntity(loc, EntityType.WITHER_SKELETON);
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
