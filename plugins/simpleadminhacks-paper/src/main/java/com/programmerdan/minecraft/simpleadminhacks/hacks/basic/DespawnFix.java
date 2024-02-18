package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Ghast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.EntitiesUnloadEvent;

/**
 Allows for high hard despawn distance and low simulation distance by forcibly removing mobs
 that are inside the simulation distance yet, still in the hard despawn distance.
 */
public class DespawnFix extends BasicHack {

	public DespawnFix(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void on(EntitiesUnloadEvent event) {
		for (Entity entity : event.getEntities()) {
			if (!(entity instanceof LivingEntity living)) {
				continue;
			}

			if (entity.getVehicle() != null) {
				continue;
			}

			if (entity instanceof EnderDragon || entity instanceof Shulker || entity instanceof Wither) {
				continue;
			}

			if (!(entity instanceof Monster || entity instanceof Ghast || entity instanceof Hoglin || entity instanceof Phantom || entity instanceof Slime)) {
				continue;
			}

			if (living.getRemoveWhenFarAway()) {
				entity.remove();
			}
		}
	}
}
