package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;

/**
 * Listener that accumulates kills of non-players by players.
 * 
 * Contributes <code>entity.death.TYPE</code> and <code>entity.death.drop.TYPE</code> and <code>entity.death.xp.TYPE</code>
 * where TYPE is the EntityType.name() of what died.
 *
 * If killer is a player and isn't empty, UUID is filled for all. Otherwise is null.
 * 
 * If creature has a custom name, recorded in string value field for death and XP; not for drop (itemstack.tostring() recorded there).
 *
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
				chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), diedName);
		this.record(death);

		if (dropped != null && dropped.size() > 0) {
			for (ItemStack drop : dropped) {
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample deathdrop = new PointDataSample("entity.death.drop." + type, this.getServer(),
						chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), dropQ.toString(), drop.getAmount());
				this.record(deathdrop);
			}
		}

		if (xpd > 0) {
			DataSample deathxp = new PointDataSample("entity.death.xp." + type, this.getServer(),
					chunk.getWorld().getName(), killerUUID, chunk.getX(), chunk.getZ(), diedName, xpd);
			this.record(deathxp);
			
		}
	}
}
