package com.devotedmc.ExilePearl.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityCombustByEntityEvent;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Helper class for entity cbust event
 * @author Gordon / ProgrammerDan
 *
 */
public class EntityCombustEventWrapper {

	private final EntityCombustByEntityEvent event;
	
	public EntityCombustEventWrapper(final EntityCombustByEntityEvent event) {
		Guard.ArgumentNotNull(event, "event");
		
		this.event = event;
	}
	
	public EntityCombustByEntityEvent getEvent() {
		return event;
	}
	
	/**
	 * Gets the damager or indirect damager from any projectile.
	 * @return The damager player
	 */
	public Player getPlayerDamager() {
		Player damager = null;
		if (event.getCombuster() instanceof Player) {
			damager = (Player) event.getCombuster();
		} else if (event.getCombuster() instanceof Wolf) {
			Wolf wolf = (Wolf) event.getCombuster();
			if (wolf.getOwner() instanceof Player) {
				damager = (Player) wolf.getOwner();
			}
		} else if (event.getCombuster() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getCombuster();
			if (projectile.getShooter() instanceof Player) {
				damager = (Player) projectile.getShooter();
			}
		}
		return damager;
	}
}
