package com.devotedmc.ExilePearl.util;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
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
		} else if (event.getDamager() instanceof Arrow) {
			Arrow arrow = (Arrow) event.getDamager();
			if (arrow.getShooter() instanceof Player) {
				damager = (Player) arrow.getShooter();
			}
		} else if (event.getDamager() instanceof Snowball) {
			Snowball snowball = (Snowball)event.getDamager();
			if (snowball.getShooter() instanceof Player) {
				damager = (Player) snowball.getShooter();
			}
		} else if (event.getDamager() instanceof Egg) {
			Egg egg = (Egg)event.getDamager();
			if (egg.getShooter() instanceof Player) {
				damager = (Player) egg.getShooter();
			}
		} else if (event.getDamager() instanceof Fireball) {
			Fireball fireball = (Fireball)event.getDamager();
			if (fireball.getShooter() instanceof Player) {
				damager = (Player) fireball.getShooter();
			}
		} else if (event.getDamager() instanceof FishHook) {
			FishHook fishHook = (FishHook)event.getDamager();
			if (fishHook.getShooter() instanceof Player) {
				damager = (Player) fishHook.getShooter();
			}
		}
		return damager;
	}
}
