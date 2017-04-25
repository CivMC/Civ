package com.programmerdan.minecraft.civspy.listeners.impl;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.civspy.DataManager;
import com.programmerdan.minecraft.civspy.DataSample;
import com.programmerdan.minecraft.civspy.PointDataSample;
import com.programmerdan.minecraft.civspy.listeners.ServerDataListener;
import com.programmerdan.minecraft.civspy.util.ItemStackToString;

import me.badbones69.crazycrates.api.PlayerPrizeEvent;

/**
 * Contributes <code>crazycrates.crate.TYPE</code> and <code>crazycrates.prize.TYPE</code> and
 * <code>crazycrates.drop.TYPE</code> stats when crazy crates awards a prize.
 * Covers all types of crates. Records which item(s) were given out in the string
 * value field for .drops, .crate stores crate name, .prize records prize name.
 * 
 * TYPE is the kind of crate -- check the CrazyCrates documentation for which values are valid.
 * 
 * @author ProgrammerDan
 *
 */
public class CrazyCratesListener extends ServerDataListener {

	public CrazyCratesListener(DataManager target, Logger logger, String server) {
		super(target, logger, server);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void givePrizeEvent(PlayerPrizeEvent event) {
		try {
			if (event.getPrize() == null || event.getCrateType() == null || event.getPrize().getItems() == null) return;
			Player player = event.getPlayer();
			UUID id = null;
			Location location = null;
			if (player != null) {
				id = player.getUniqueId();
				location = player.getLocation();
			}
			
			Chunk chunk = player != null ? location.getChunk() : null;
			
			// record Crate
			// record Prize
			// record Items (drops)
			DataSample crate = new PointDataSample("crazycrates.crate." + event.getCrateType(), this.getServer(),
					chunk != null ? chunk.getWorld().getName() : null, player != null ? id : null,
					chunk != null ? chunk.getX() : null, chunk != null ? chunk.getZ() : null,
					event.getCrateName());
			this.record(crate);
			
			DataSample prize = new PointDataSample("crazycrates.prize." + event.getCrateType(), this.getServer(),
					chunk != null ? chunk.getWorld().getName() : null, player != null ? id : null,
					chunk != null ? chunk.getX() : null, chunk != null ? chunk.getZ() : null,
					event.getPrize().getName());
			this.record(prize);

			for (ItemStack drop : event.getPrize().getItems()) {
				if (drop == null) continue;
				ItemStack dropQ = drop.clone();
				dropQ.setAmount(1);
				DataSample rdrop = new PointDataSample("crazycrates.drop." + event.getCrateType(), this.getServer(),
						chunk != null ? chunk.getWorld().getName() : null, player != null ? id : null,
						chunk != null ? chunk.getX() : null, chunk != null ? chunk.getZ() : null,
						ItemStackToString.toString(dropQ), drop.getAmount());
				this.record(rdrop);
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Failed to spy a crazycrates drop event", e);
		}
	}

	@Override
	public void shutdown() {
		// NO-OP
	}

}
