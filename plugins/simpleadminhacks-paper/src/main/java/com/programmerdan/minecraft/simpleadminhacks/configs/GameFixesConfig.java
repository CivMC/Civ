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
	private ArrayList<Material> matArray;

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
		if(stopRailDupe) plugin().log("Stop Rail Dupe is enabled.");

		stopEndPortalDeletion = config.getBoolean("stopEndPortalDeletion", true);
		if (stopEndPortalDeletion) plugin().log("Stop End Portal Deletion is enabled.");
	}

	private void wireUpArrays()
	{
		bfArray = new ArrayList<BlockFace>();
		matArray = new ArrayList<Material>();

		matArray.add(Material.RAILS);
		matArray.add(Material.ACTIVATOR_RAIL);
		matArray.add(Material.DETECTOR_RAIL);
		matArray.add(Material.POWERED_RAIL);
		matArray.add(Material.CARPET);

		bfArray.add(BlockFace.NORTH);
		bfArray.add(BlockFace.SOUTH);
		bfArray.add(BlockFace.EAST);
		bfArray.add(BlockFace.WEST);
		bfArray.add(BlockFace.UP);
		bfArray.add(BlockFace.DOWN);
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

	public boolean isStopRailDupe()
	{
		return stopRailDupe;
	}

	public boolean isStopEndPortalDeletion()
	{
		return stopEndPortalDeletion;
	}

	public ArrayList<BlockFace> getBfArray()
	{
		return bfArray;
	}

	public ArrayList<Material> getMatArray()
	{
		return matArray;
	}
}