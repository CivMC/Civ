package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

import vg.civcraft.mc.citadel.events.ReinforcementAcidBlockedEvent;
import vg.civcraft.mc.citadel.events.ReinforcementChangeTypeEvent;
import vg.civcraft.mc.citadel.events.ReinforcementCreationEvent;
import vg.civcraft.mc.citadel.events.ReinforcementDamageEvent;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.citadel.reinforcementtypes.ReinforcementType;
import vg.civcraft.mc.namelayer.group.Group;

/**
 * Sample Listener class that records all citadel damage and break for summation
 * by who and what.
 * 
 * @author ProgrammerDan
 */
public final class CitadelListener extends ServerDataListener {

	public CitadelListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

	/**
	 * Generates: <code>player.citadel.damage.TYPE</code> stat_key data. Block
	 * encoded attributes is stored in the string value field. When reinforcement is
	 * damaged.
	 * 
	 * <code>player.citadel.damage.group</code> stat_key data. Group name is stored
	 * in the string value field.
	 * 
	 * @param event The Damage event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void damageListen(ReinforcementDamageEvent event) {
		try {
			Player p = event.getPlayer();
			if (p == null) {
				return;
			}
			UUID id = p.getUniqueId();
			Reinforcement reinf = event.getReinforcement();
			Block block = reinf.getLocation().getBlock();
			Chunk chunk = block.getChunk();
			String type = reinf.getType().getItem().toString();
			Group group = reinf.getGroup();

			if (group != null) {
				DataSample groupDamage = new PointDataSample("player.citadel.damage.group", this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), group.getName());
				this.record(groupDamage);
			}

			DataSample reinfDamage = new PointDataSample("player.citadel.damage." + type, this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(),
					ItemStackToString.toString(block.getState()));
			this.record(reinfDamage);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a citadel damage event", e);
		}
	}

	/**
	 * Generates: <code>player.citadel.create.TYPE</code> stat_key data. Block
	 * encoded attributes is stored in the string value field. When reinforcement is
	 * created.
	 * 
	 * <code>player.citadel.create.group</code> stat_key data. Group used it stored
	 * in the string value field.
	 * 
	 * @param event The Create event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void createListen(ReinforcementCreationEvent event) {
		try {
			Player p = event.getPlayer();
			if (p == null) {
				return;
			}
			UUID id = p.getUniqueId();
			Block broken = event.getReinforcement().getLocation().getBlock();
			Chunk chunk = broken.getChunk();

			Reinforcement reinf = event.getReinforcement();

			String type = reinf.getType().getItem().toString();
			Group group = reinf.getGroup();
			if (group != null) {
				DataSample groupDamage = new PointDataSample("player.citadel.create.group", this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), group.getName());
				this.record(groupDamage);
			}

			DataSample reinfCreate = new PointDataSample("player.citadel.create." + type, this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(),
					ItemStackToString.toString(broken.getState()));
			this.record(reinfCreate);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a citadel create event", e);
		}
	}

	/**
	 * Generates: <code>player.citadel.change.TYPE</code> stat_key data. Block
	 * encoded attributes is stored in the string value field. When reinforcement is
	 * created.
	 * 
	 * @param event The Create event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void changeListen(ReinforcementChangeTypeEvent event) {
		try {
			Player p = event.getPlayer();
			if (p == null) {
				return;
			}
			UUID id = p.getUniqueId();

			ReinforcementType rtype = event.getNewType();
			Reinforcement reinf = event.getReinforcement();

			Location block = reinf.getLocation();
			if (block == null) {
				return;
			}
			Chunk chunk = block.getChunk();

			String type = reinf.getType().getItem().toString();
			Group group = reinf.getGroup();

			if (group != null) {
				DataSample groupDamage = new PointDataSample("player.citadel.change.group", this.getServer(),
						chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(), group.getName());
				this.record(groupDamage);
			}

			DataSample reinfChange = new PointDataSample("player.citadel.change." + type, this.getServer(),
					chunk.getWorld().getName(), id, chunk.getX(), chunk.getZ(),
					ItemStackToString.toString(rtype.getItem()));
			this.record(reinfChange);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a citadel change event", e);
		}
	}

	/**
	 * Generates: <code>player.citadel.acid.TYPE</code> stat_key data. Block encoded
	 * attributes is stored in the string value field. When reinforcement is
	 * created.
	 * 
	 * @param event The Create event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void acidListen(ReinforcementAcidBlockedEvent event) {
		try {
			Reinforcement destroyed = event.getReinforcement();
			Reinforcement acid = event.getAcidBlockReinforcement();

			Location block = destroyed.getLocation();
			Chunk chunk = block.getChunk();

			String typeDestroyed = ItemStackToString.toString(destroyed.getType().getItem());
			String typeAcid = ItemStackToString.toString(acid.getType().getItem());

			DataSample reinfCreate = new PointDataSample("player.citadel.acid." + typeAcid, this.getServer(),
					chunk.getWorld().getName(), null, chunk.getX(), chunk.getZ(), typeDestroyed);
			this.record(reinfCreate);

			Group group = destroyed.getGroup();

			if (group != null) {
				DataSample groupDamage = new PointDataSample("player.citadel.acid.destroyed.group", this.getServer(),
						chunk.getWorld().getName(), null, chunk.getX(), chunk.getZ(), group.getName());
				this.record(groupDamage);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a citadel acid event", e);
		}
	}
}
