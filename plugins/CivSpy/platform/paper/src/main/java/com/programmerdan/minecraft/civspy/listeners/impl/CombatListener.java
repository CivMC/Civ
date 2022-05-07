package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.projectiles.ProjectileSource;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Records the nature of combat damage issued as delivered by players.
 * To simplify presentation, it is strictly recorded as such:
 * <code>player.damage.TYPE</code> where TYPE is nature of damage dealt, as discernable.
 * uuid is the player delivering the damage; string_value is the player or entity receiving
 * the damage; and numeric_value is the damage dealt.
 * 
 * Finally this also tracks damage received for non-combat means (falls, etc.) as follows:
 * <code>player.damaged</code> with string_value being the nature of the damage received,
 * and numeric_value the damage amount.
 * 
 * @author <a href="mailto:programmerdan@gmail.com">ProgrammerDan</a>
 */
public final class CombatListener extends ServerDataListener {
	
	public CombatListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
	}
	
	/**
	 * Generates: <code>player.damage.TYPE</code> stat_key data. 
	 * Where TYPE is nature of damage dealt, as discernable.
	 * uuid is the player delivering the damage; string_value is the player or entity receiving
	 * the damage; and numeric_value is the damage dealt.
	 * 
	 * @param event The EntityDamageEvent to record.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void GeneralDamageListener(EntityDamageEvent event) {
		if (event instanceof EntityDamageByEntityEvent || event instanceof EntityDamageByBlockEvent ) {
			return;
		}
		Entity whoGotHurt = event.getEntity();
		if (!(whoGotHurt instanceof HumanEntity)) {
			return;
		}
		
		doDamage(whoGotHurt, event);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) 
	public void entityOnEntityDamageListener(EntityDamageByEntityEvent event) {
		Entity whoGotHurt = event.getEntity();
		if (!(whoGotHurt instanceof HumanEntity)) {
			return;
		}
		
		doDamage(whoGotHurt, event);
	}

	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true) 
	public void blockOnEntityDamageListener(EntityDamageByBlockEvent event) {
		Entity whoGotHurt = event.getEntity();
		if (!(whoGotHurt instanceof HumanEntity)) {
			return;
		}
		
		doDamage(whoGotHurt, event);
	}
	
	private void doDamage(Entity whoGotHurt, EntityDamageEvent event) {
		HumanEntity p = (HumanEntity) whoGotHurt;
		UUID id = p.getUniqueId();
		Location loc = p.getLocation();
		Chunk chunk = loc.getChunk();
		String world = chunk.getWorld().getName();
		DamageCause cause = event.getCause();
		double damage = event.getFinalDamage();
		String damagerName = null;
		
		if (event instanceof EntityDamageByEntityEvent) {
			Entity killer = ((EntityDamageByEntityEvent) event).getDamager();
			if (killer != null) {
				if (killer instanceof Projectile) {
					// Projectiles do the killing, but they are launched by someone!
					Projectile arrow = (Projectile) killer;
					ProjectileSource ps = arrow.getShooter();
					if (ps instanceof HumanEntity) {
						HumanEntity hek = (HumanEntity) ps;
						damagerName = hek.getUniqueId().toString();
					} else if (ps instanceof Entity) {
						Entity psk = (Entity) ps;
						damagerName = psk.getCustomName() != null ? psk.getCustomName() : psk.getType().toString();
					} else {
						damagerName = "ProjectileLauncher-" + killer.getType().toString(); 
					}
				} else if (killer instanceof LivingEntity) {
					damagerName = ((LivingEntity) killer).getUniqueId().toString();
				}
				if (damagerName == null) {
					damagerName = killer.getCustomName() != null ? killer.getCustomName() : killer.getType().toString();
				}
			} else {
				damagerName = "Unknown";
			}
			DataSample damaged = new PointDataSample("player.damage." + cause.toString(), this.getServer(), world, id,
					chunk.getX(), chunk.getZ(), damagerName, damage);
			this.record(damaged);				
		} else if (event instanceof EntityDamageByBlockEvent) {
			EntityDamageByBlockEvent edbe = (EntityDamageByBlockEvent) event;
			Block block = edbe.getDamager();
			if (block != null) {
				damagerName = ItemStackToString.toString(block.getState());
			} else {
				damagerName = "Block";
			}
			DataSample damaged = new PointDataSample("player.damage." + cause.toString(), this.getServer(), world, id,
					chunk.getX(), chunk.getZ(), cause.toString(), damage);
			this.record(damaged);	
		} else {
			DataSample damaged = new PointDataSample("player.damaged", this.getServer(), world, id,
					chunk.getX(), chunk.getZ(), cause.toString(), damage);
			this.record(damaged);
		}
	}
}
