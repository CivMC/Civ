package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.Iterator;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
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
		PigZombie pigZombie = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
		pigZombie.getEquipment().setItemInOffHand(new ItemStack(Material.GHAST_TEAR));
	}
	
	private void spawnWitherSkeleton(Location loc) {
		PigZombie pigZombie = (PigZombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIFIED_PIGLIN);
		pigZombie.getEquipment().setHelmet(new ItemStack(Material.WITHER_SKELETON_SKULL));
	}
	
	private boolean roll(double chance) {
		return rng.nextDouble() <= chance;
	}
	
	@EventHandler
	public void onEntityPickupItem(EntityPickupItemEvent e) {
		if (e.getEntityType() == EntityType.ZOMBIFIED_PIGLIN) {
			e.setCancelled(true); // Prevents giving piglins items to change what they drop
		}
	}

	@EventHandler
	public void dispenseArmorEvent(BlockDispenseArmorEvent e) {
		if (e.getTargetEntity().getType() == EntityType.ZOMBIFIED_PIGLIN) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void zombiePiglinDeath(EntityDeathEvent e) {
		if (e.getEntityType() != EntityType.ZOMBIFIED_PIGLIN) { // If the death is not a piglin, we can disregard.
			return;
		}

		EntityType type = EntityType.ZOMBIFIED_PIGLIN; // Default type is piglin

		//Iterating through the drops to remove the added wither skull & ghast tear, if present.
		Iterator<ItemStack> iter = e.getDrops().iterator();
		while(iter.hasNext()) {
			ItemStack is = iter.next();
			if (is.getType() == Material.WITHER_SKELETON_SKULL) {
				iter.remove();
			} else if (is.getType() == Material.GHAST_TEAR) {
				iter.remove();
			}
		}

		if (e.getEntity().getEquipment().getHelmet().getType() == Material.WITHER_SKELETON_SKULL) {
			type = EntityType.WITHER_SKELETON;
		} else if (e.getEntity().getEquipment().getItemInOffHand().getType() == Material.GHAST_TEAR) {
			type = EntityType.GHAST;
		}

		if (type != EntityType.ZOMBIFIED_PIGLIN) { // Type is a mock-wither or mock-ghast
			e.getDrops().clear(); // Clear piglin drops

			if (type == EntityType.GHAST) {
				int amount = rng.nextInt(2); // Random drop of 0 or 1 Ghast Tear
				if (amount > 0) {
					e.getDrops().add(new ItemStack(Material.GHAST_TEAR, amount));
				}

				amount = rng.nextInt(3); // Random drop of 0, 1, or 2 Gunpowder
				if (amount > 0) {
					e.getDrops().add(new ItemStack(Material.GUNPOWDER, amount));
				}
			}
			else { //WITHER
				int amount = rng.nextInt(3); // Random drop of 0, 1, or 2 Bones
				if (amount > 0) {
					e.getDrops().add(new ItemStack(Material.BONE, amount));
				}

				amount = rng.nextInt(3) -1; // Random drop of -1, 0, or 1 Coal. (1/3 chance for a single coal)
				if (amount > 0) {
					e.getDrops().add(new ItemStack(Material.COAL, amount));
				}

				if (roll(witherHeadDropChance)) { // Standard role for wither skull chance.
					e.getDrops().add(new ItemStack(Material.WITHER_SKELETON_SKULL));
				}
			}
		}
	}
}
