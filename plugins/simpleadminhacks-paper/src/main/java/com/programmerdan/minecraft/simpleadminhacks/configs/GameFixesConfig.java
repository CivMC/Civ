package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

import java.util.ArrayList;

public class GameFixesConfig extends SimpleHackConfig {

	private boolean blockElytraBreakBug;
	private double damageOnElytraBreakBug;
	private boolean canStorageTeleport;
	private boolean stopHopperDupe;
	private boolean stopRailDupe;
	private boolean stopEndPortalDeletion;

	private ArrayList<BlockFace> bfArray;
	private ArrayList<Material> railArray;
	private ArrayList<Material> pistonArray;

	public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
		wireUpArrays();
	}

	protected void wireup(ConfigurationSection config) {
		blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
		damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
		canStorageTeleport = config.getBoolean("canStorageTeleport");
		stopHopperDupe = config.getBoolean("stopHopperDupe");

		stopRailDupe = config.getBoolean("stopRailDupe", true);
		if (stopRailDupe) plugin().log("Stop Rail Dupe is enabled.");

		stopEndPortalDeletion = config.getBoolean("stopEndPortalDeletion", true);
		if (stopEndPortalDeletion) plugin().log("Stop End Portal Deletion is enabled.");
	}

	private void wireUpArrays() {
		bfArray = new ArrayList<BlockFace>();
		railArray = new ArrayList<Material>();
		pistonArray = new ArrayList<Material>();

		railArray.add(Material.RAILS);
		railArray.add(Material.ACTIVATOR_RAIL);
		railArray.add(Material.DETECTOR_RAIL);
		railArray.add(Material.POWERED_RAIL);
		railArray.add(Material.CARPET);

		bfArray.add(BlockFace.NORTH);
		bfArray.add(BlockFace.SOUTH);
		bfArray.add(BlockFace.EAST);
		bfArray.add(BlockFace.WEST);
		bfArray.add(BlockFace.UP);
		bfArray.add(BlockFace.DOWN);

		pistonArray.add(Material.PISTON_BASE);
		pistonArray.add(Material.PISTON_EXTENSION);
		pistonArray.add(Material.PISTON_MOVING_PIECE);
		pistonArray.add(Material.PISTON_STICKY_BASE);
	}

	public boolean isBlockElytraBreakBug() {
		return blockElytraBreakBug;
	}

	public double getDamageOnElytraBreakBug() {
		return damageOnElytraBreakBug;
	}

	public boolean canStorageTeleport() {
		return canStorageTeleport;
	}

	public boolean isStopHopperDupe() {
		return stopHopperDupe;
	}

	public boolean isStopRailDupe() {
		return stopRailDupe;
	}

	public boolean isStopEndPortalDeletion() {
		return stopEndPortalDeletion;
	}

	public ArrayList<BlockFace> getBfArray() {
		return bfArray;
	}

	public ArrayList<Material> getRailArray() {
		return railArray;
	}

	public ArrayList<Material> getPistonArray() {
		return pistonArray;
	}
}