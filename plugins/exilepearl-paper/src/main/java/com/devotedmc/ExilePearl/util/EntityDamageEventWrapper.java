package com.devotedmc.ExilePearl.util;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Wolf;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import vg.civcraft.mc.civmodcore.util.Guard;

/**
 * Helper class for entity damage event
 * @author Gordon
 *
 */
public class EntityDamageEventWrapper {

	private final EntityDamageByEntityEvent event;
	
	public EntityDamageEventWrapper(final EntityDamageByEntityEvent event) {
		Guard.ArgumentNotNull(event, "event");
		
		this.event = event;
	}
	
	public EntityDamageByEntityEvent getEvent() {
		return event;
	}
	
	/**
	 * Gets the damager or indirect damager from any projectile.
	 * @return The damager player
	 */
	public Player getPlayerDamager() {
		Player damager = null;
		if (event.getDamager() instanceof Player) {
			damager = (Player) event.getDamager();
		} else if (event.getDamager() instanceof Wolf) {
			Wolf wolf = (Wolf) event.getDamager();
			if (wolf.getOwner() instanceof Player) {
				damager = (Player) wolf.getOwner();
			}
		} else if (event.getDamager() instanceof Projectile) {
			Projectile projectile = (Projectile) event.getDamager();
			if (projectile.getShooter() instanceof Player) {
				damager = (Player) projectile.getShooter();
			}
		}
		return damager;
	}
}
