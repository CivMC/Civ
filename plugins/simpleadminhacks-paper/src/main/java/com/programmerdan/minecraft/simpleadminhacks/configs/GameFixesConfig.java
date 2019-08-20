package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.ArrayList;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

public class GameFixesConfig extends SimpleHackConfig {

	private boolean blockElytraBreakBug;
	private double damageOnElytraBreakBug;
	private boolean canStorageTeleport;
	private boolean stopHopperDupe;
	private boolean stopRailDupe;
	private boolean stopEndPortalDeletion;
	private boolean stopBedBombing;

	private ArrayList<BlockFace> bfArray;
	private ArrayList<Material> railArray;
	private ArrayList<Material> pistonArray;

	private boolean preventTreeWrap;
	private boolean maintainFlatBedrock;
	private boolean fixPearlGlitch;
	private boolean preventLongSigns;
	private int signLengthLimit;
	private boolean preventLongSignsAbsolute;
	private boolean cancelLongSignEvent;

	public GameFixesConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
		wireUpArrays();
	}

	protected void wireup(ConfigurationSection config) {
		blockElytraBreakBug = config.getBoolean("blockElytraBreakBug", true);
		damageOnElytraBreakBug = config.getDouble("damageOnElytraBreakBug", 0.0d);
		if (blockElytraBreakBug) plugin().log(Level.INFO, "  Block Elytra 1height break bug is enabled, doing {} damage to violators", damageOnElytraBreakBug);

		canStorageTeleport = config.getBoolean("canStorageTeleport");
		if (!canStorageTeleport) plugin().log("  Storage holder teleportation is disabled.");

		stopHopperDupe = config.getBoolean("stopHopperDupe");
		if (stopHopperDupe) plugin().log("  Stop Hopper self-feeding Dupe is enabled.");

		stopRailDupe = config.getBoolean("stopRailDupe", true);
		if (stopRailDupe) plugin().log("  Stop Rail Dupe is enabled.");

		stopEndPortalDeletion = config.getBoolean("stopEndPortalDeletion", true);
		if (stopEndPortalDeletion) plugin().log("  Stop End Portal Deletion is enabled.");

		stopBedBombing = config.getBoolean("stopBedBombingInHellBiomes", true);
		if (stopBedBombing) plugin().log("  Stop Bed Bombing In Hell Biomes is enabled.");

		preventTreeWrap = config.getBoolean("preventTreeWraparound", true);
		if (preventTreeWrap) plugin().log("  Stop tree wrapping into bedrock is enabled.");

		maintainFlatBedrock = config.getBoolean("maintainFlatBedrock", false);
		if (maintainFlatBedrock) plugin().log("  Maintaining bedrock flatness.");

		fixPearlGlitch = config.getBoolean("fixPearlGlitch", false);
		if (fixPearlGlitch) plugin().log("  Pearl glitch fix enabled.");

		preventLongSigns = config.getBoolean("preventLongSigns", true);
		signLengthLimit = config.getInt("signLengthLimit", 100);
		preventLongSignsAbsolute = config.getBoolean("preventLongSignsAbsolute", true);
		cancelLongSignEvent = config.getBoolean("cancelLongSignEvent", false);
	}

	private void wireUpArrays() {
		bfArray = new ArrayList<>();
		railArray = new ArrayList<>();
		pistonArray = new ArrayList<>();

		railArray.add(Material.RAIL);
		railArray.add(Material.ACTIVATOR_RAIL);
		railArray.add(Material.DETECTOR_RAIL);
		railArray.add(Material.POWERED_RAIL);
		railArray.add(Material.WHITE_CARPET);
		railArray.add(Material.RED_CARPET);
		railArray.add(Material.BLACK_CARPET);
		railArray.add(Material.BLUE_CARPET);
		railArray.add(Material.BROWN_CARPET);
		railArray.add(Material.CYAN_CARPET);
		railArray.add(Material.GRAY_CARPET);
		railArray.add(Material.GREEN_CARPET);
		railArray.add(Material.LIGHT_BLUE_CARPET);
		railArray.add(Material.LIGHT_GRAY_CARPET);
		railArray.add(Material.LIME_CARPET);
		railArray.add(Material.YELLOW_CARPET);
		railArray.add(Material.PURPLE_CARPET);
		railArray.add(Material.PINK_CARPET);
		railArray.add(Material.ORANGE_CARPET);
		railArray.add(Material.MAGENTA_CARPET);

		bfArray.add(BlockFace.NORTH);
		bfArray.add(BlockFace.SOUTH);
		bfArray.add(BlockFace.EAST);
		bfArray.add(BlockFace.WEST);
		bfArray.add(BlockFace.UP);
		bfArray.add(BlockFace.DOWN);

		pistonArray.add(Material.PISTON);
		pistonArray.add(Material.PISTON_HEAD);
		pistonArray.add(Material.MOVING_PISTON);
		pistonArray.add(Material.STICKY_PISTON);
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

	public boolean stopBedBombing() {
		return this.stopBedBombing;
	}

	public boolean stopTreeWraparound() {
		return preventTreeWrap;
	}

	public boolean maintainFlatBedrock() {
		return maintainFlatBedrock;
	}

	public boolean fixPearlGlitch() {
		return fixPearlGlitch;
	}

	public boolean isPreventLongSigns() {
		return preventLongSigns;
	}

	public int getSignLengthLimit() {
		return signLengthLimit;
	}

	public boolean isPreventLongSignsAbsolute() {
		return preventLongSignsAbsolute;
	}

	public boolean isCancelLongSignEvent() {
		return cancelLongSignEvent;
	}

}