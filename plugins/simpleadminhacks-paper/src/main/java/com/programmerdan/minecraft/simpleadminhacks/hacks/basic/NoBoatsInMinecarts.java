package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;

/**
 * Prevents boats from entering minecarts.
 * Fix for <a href="https://bugs.mojang.com/browse/MC-113871">MC-113871</a>
 */
public class NoBoatsInMinecarts extends BasicHack {

	public NoBoatsInMinecarts(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@EventHandler
	public void on(VehicleEntityCollisionEvent e) {
		if (e.getVehicle() instanceof Minecart m && e.getEntity() instanceof Boat b
				&& !b.isInsideVehicle() && m.getPassengers().isEmpty()) {
			e.setCancelled(true);
		}
	}
}
