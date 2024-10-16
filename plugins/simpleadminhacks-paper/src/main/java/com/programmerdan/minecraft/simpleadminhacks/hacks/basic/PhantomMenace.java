package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerStatisticIncrementEvent;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public final class PhantomMenace extends BasicHack {

	@AutoLoad
	private int timeSinceRestCap;

	@AutoLoad
	private boolean nightSpawn;

	@AutoLoad
	private boolean stormSpawn;

	@AutoLoad
	private int maximumLightSpawn;

	public PhantomMenace(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		this.timeSinceRestCap = Math.max(-1, this.timeSinceRestCap);
		this.maximumLightSpawn = Math.min(Math.max(this.maximumLightSpawn, 0), 15);
	}

	@EventHandler
	public void onCreatureSpawn(final CreatureSpawnEvent event) {
		// Do not regulate unless the creature is a phantom
		if (event.getEntityType() != EntityType.PHANTOM) {
			return;
		}
		// Do not regulate phantom spawning unless it's natural
		switch (event.getSpawnReason()) {
			case NATURAL:
			case DEFAULT:
				break;
			default:
				return;
		}
		if (!this.nightSpawn && !this.stormSpawn) {
			event.setCancelled(true);
			plugin().debug("Phantom prevented from spawning: cannot spawn during the night nor during storms.");
			return;
		}
		final Phantom phantom = (Phantom) event.getEntity();
		final World world = phantom.getWorld();
		if (!this.nightSpawn && this.stormSpawn) {
			if (!world.hasStorm()) {
				event.setCancelled(true);
				plugin().debug("Phantom prevented from spawning: night spawning disabled.");
			}
		}
		else if (this.nightSpawn && !this.stormSpawn) {
			if (world.getTime() < 12300 || world.getTime() > 23850) {
				event.setCancelled(true);
				plugin().debug("Phantom prevented from spawning: storm spawning disabled.");
			}
		}
		final UUID targetUUID = phantom.getSpawningEntity();
		if (targetUUID != null) {
			final Entity target = Bukkit.getEntity(targetUUID);
			if (target != null) {
				final Location targetLocation = target.getLocation();
				if (WorldUtils.isBlockLoaded(targetLocation)) {
					final Block targetBlock = targetLocation.getBlock();
					final int lightLevel = targetBlock.getLightLevel();
					if (lightLevel > this.maximumLightSpawn) {
						event.setCancelled(true);
						plugin().debug("Phantom prevented from spawning: light level was too high.");
					}
				}
			}
		}
		// Otherwise let the phantom(s) spawn
	}

	@EventHandler
	public void onStatisticIncrease(final PlayerStatisticIncrementEvent event) {
		if (event.getStatistic() != Statistic.TIME_SINCE_REST) {
			return;
		}
		final Player player = event.getPlayer();
		if (event.getPreviousValue() < 72_000 && event.getNewValue() >= 72_000) {
			player.sendMessage(ChatColor.DARK_RED + "You haven't slept in a while...");
		}
		final int cap = this.timeSinceRestCap;
		if (cap <= -1 || event.getNewValue() < cap) {
			return;
		}
		player.setStatistic(Statistic.TIME_SINCE_REST, Math.min(event.getNewValue(), cap));
		event.setCancelled(true); // Prevent the increment
	}

}
