package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

/**
 * Listener that accumulates kills of non-players by players, and players by entities (including players).
 * <br>
 * Contributes <code>entity.death.TYPE</code> and <code>entity.death.drop.TYPE</code> and <code>entity.death.xp.TYPE</code>
 * where TYPE is the EntityType.name() of what died.
 * <br>
 * If killer is a player and isn't empty, UUID is filled for all. Otherwise is null.
 * <br>
 * If creature has a custom name, recorded in string value field for death and XP; not for drop (itemstack serialization recorded there).
 * <br>
 * Contributes <code>player.killed</code> and <code>player.died</code> and <code>player.killed.drop</code> and <code>player.died.drop</code>
 * <code>.killed</code> is used for PVP kills or death by entities, the UUID of the player or TYPE/Name of the entity responsible for killing is in hte
 * string value field. The UUID field holds the player killed in all cases. If <code>.died</code> then killed by 
 * some non-strictly-entity cause (drowning, etc.).
 * The <code>.drop</code> contributions are similar to entity death, but the UUID recorded is the player that died.
 * @author ProgrammerDan
 */
public final class HuntingListener extends ServerDataListener {

	public HuntingListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@Override
	public void shutdown() {
		// NO-OP
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void capturePlayerDeath(PlayerDeathEvent event) {
		Player died = event.getEntity();
		if (died == null) return;
		
		UUID playerUUID = died.getUniqueId();
		
		Location location = died.getLocation();
		Chunk chunk = location.getChunk();
		
		String killerName = null;
		boolean killerIsEntity = false;
		if (died.getKiller() != null) {
			Player killer = died.getKiller();
			killerName = killer.getUniqueId().toString();
			killerIsEntity = true;
		} else if (died.getLastDamageCause() != null) {
			EntityDamageEvent ede = died.getLastDamageCause();
			if (ede instanceof EntityDamageByEntityEvent) {
				Entity killer = ((EntityDamageByEntityEvent) ede).getDamager();
				if (killer != null) {
					killerName = killer.getCustomName() != null ? killer.getCustomName() : killer.getType().toString();
				} else {
					killerName = "Unknown";
				}
				killerIsEntity = true;
			} else {
				killerName = ede.getCause().toString();
			}
		}
		
		DataSample death = new PointDataSample("player." + (killerIsEntity ? "killed" : "died"), this.getServer(),
					chunk.getWorld().getName(), playerUUID, chunk.getX(), chunk.getZ(), 
					killerName);
		this.record(death);
		
		List<ItemStack> dropped = event.getDrops();
		
		if (dropped != null && dropped.size() > 0) {
			for (ItemStack drop : dropped) {
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample deathdrop = new PointDataSample("player." + (killerIsEntity ? "killed.drop" : "died.drop"),
						this.getServer(), chunk.getWorld().getName(), playerUUID, chunk.getX(), chunk.getZ(), 
						ItemStackToString.toString(dropQ), drop.getAmount());
				this.record(deathdrop);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void captureDeath(EntityDeathEvent event) {
		if (event instanceof PlayerDeathEvent || event.getEntity() == null || event.getEntity() instanceof Player) return;

		LivingEntity died = event.getEntity();
		List<ItemStack> dropped = event.getDrops();
		int xpd = event.getDroppedExp();

		Player killer = died.getKiller();
		UUID killerUUID = killer == null ? null : killer.getUniqueId();

		Location location = died.getLocation();
		Chunk chunk = location.getChunk();

		String type = died.getType().name();
		String diedCName = died.getCustomName();
		
		DataSample death = new PointDataSample("entity.death." + type, this.getServer(),
				chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), diedCName);
		this.record(death);

		if (dropped != null && dropped.size() > 0) {
			for (ItemStack drop : dropped) {
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample deathdrop = new PointDataSample("entity.death.drop." + type, this.getServer(),
						chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), 
						ItemStackToString.toString(dropQ), drop.getAmount());
				this.record(deathdrop);
			}
		}

		if (xpd > 0) {
			DataSample deathxp = new PointDataSample("entity.death.xp." + type, this.getServer(),
					chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), diedCName, xpd);
			this.record(deathxp);
			
		}
	}
}
