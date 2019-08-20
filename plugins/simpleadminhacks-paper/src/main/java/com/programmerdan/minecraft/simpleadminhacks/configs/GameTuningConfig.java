package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Holds configurations for the GameTuning module.
 *
 * @author ProgrammerDan
 */
public class GameTuningConfig extends SimpleHackConfig {

	private boolean chunkLimitsEnabled;
	private Map<Material, Integer> blockEntityLimits;
	private Set<UUID> exemptFromLimits;
	private String chunkLimitsExceededMessage;

	private boolean daytimeBedEnabled;
	private String daytimeBedSpawnSetMessage;

	private boolean oneToOneNether;
	private boolean returnNetherPortal;
	private boolean allowNetherTravel;

	private boolean chestedMinecartInventories;
	private boolean hopperMinecartInventories;
	private boolean enderChestInventories;

	private boolean stopTrapHorses;
	private boolean killTrapHorses;

	private boolean changeSpawnerType;

	private boolean allowVillagerTrading;

	private boolean enderGrief;
	private boolean witherGrief;

	private boolean preventFallingThroughBedrock;

	private Set<Material> noPlace;

	public GameTuningConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		ConfigurationSection chunkLimits = config.getConfigurationSection("chunkLimits");
		wireupChunkLimits(chunkLimits);

		ConfigurationSection daytimeBed = config.getConfigurationSection("daytimeBed");
		wireupDaytimeBed(daytimeBed);

		this.oneToOneNether = config.getBoolean("oneToOneNether", false);
		if (oneToOneNether) plugin().log("One to One Nether is enabled.");

		this.returnNetherPortal = config.getBoolean("returnNetherPortal", true);
		if (!returnNetherPortal) plugin().log("Return Nether Portals disabled.");

		this.allowNetherTravel = config.getBoolean("allowNetherTravel", false);
		if (!allowNetherTravel) plugin().log("Nether travel disabled.");

		this.chestedMinecartInventories = config.getBoolean("chestedMinecartInventories", true);
		if (!chestedMinecartInventories) plugin().log("Chested Minecart Inventories are disabled.");

		this.hopperMinecartInventories = config.getBoolean("hopperMinecartInventories", true);
		if (!hopperMinecartInventories) plugin().log("Hopper Minecart Inventories are disabled.");

		this.enderChestInventories = config.getBoolean("enderChestInventories", false);
		if (!enderChestInventories) plugin().log("Ender chest inventories are disabled.");

		this.stopTrapHorses = config.getBoolean("stopTrapHorses", true);
		if (stopTrapHorses) plugin().log("Stopping trap horses from being annoying.");

		this.killTrapHorses = config.getBoolean("killTrapHorses", true);
		if (killTrapHorses) plugin().log("Killing trap horses as well");

		this.changeSpawnerType = config.getBoolean("changeSpawnerType", false);
		if (!changeSpawnerType) plugin().log("Spawner type changing disabled");

		this.allowVillagerTrading = config.getBoolean("allowVillagerTrading", false);
		if (!allowVillagerTrading) plugin().log("Villager trading disabled");

		this.enderGrief = config.getBoolean("enderGrief", false);
		if (!enderGrief) plugin().log("Ender grief is disabled.");

		this.witherGrief = config.getBoolean("witherGrief", false);
		if (!witherGrief) plugin().log("Wither grief is disabled.");

		this.preventFallingThroughBedrock = config.getBoolean("preventFallingThroughBedrock", true);

		noPlace = new HashSet<>();
		if(config.isList("noplace")) {
			for(String entry : config.getStringList("noplace")) {
				try {
					noPlace.add(Material.valueOf(entry));
				}
				catch (IllegalArgumentException e) {
					plugin().log(Level.WARNING, "Material " + entry + " at " + config.getCurrentPath() + " could not be parsed");
				}
			}
		}
		/* Add additional tuning config grabs here. */
	}

	/**
	 * Wireup for Chunk Limits configuration
	 *
	 * @author ProgrammerDan
	 */
	private void wireupChunkLimits(ConfigurationSection config) {
		this.blockEntityLimits = new HashMap<>();
		this.exemptFromLimits = new HashSet<>();
		this.chunkLimitsEnabled = false;
		if (config == null) return;

		this.chunkLimitsEnabled = config.getBoolean("enabled", false);
		this.chunkLimitsExceededMessage = ChatColor.translateAlternateColorCodes('&',
				config.getString("exceededMessage",
						ChatColor.RED + "Limit for this chunk reached, you cannot place that! Use a different block."));

		// for each "chunkLimits.tileEntities: " entry, record limit.
		ConfigurationSection tileEntities = config.getConfigurationSection("tileEntities");
		for (String key : tileEntities.getKeys(false)) {
			int limit = tileEntities.getInt(key);
			Material toBlock = Material.getMaterial(key);
			if (toBlock == null) continue;

			this.blockEntityLimits.put(toBlock, limit);
			plugin().log(Level.INFO, " Limiting {0} to {1} per chunk", toBlock.toString(), limit);
		}

		// for each "chunkLimits.exempt: " entry, record limit.
		List<String> exempts = config.getStringList("exempt");
		for (String exempt : exempts) {
			UUID pExempt = null;
			try {
				pExempt = UUID.fromString(exempt);
			} catch (IllegalArgumentException iae) {
				pExempt = null;
			}
			if (pExempt == null) {
				OfflinePlayer ofp = Bukkit.getOfflinePlayer(exempt);
				if (ofp != null) {
					pExempt = ofp.getUniqueId();
				}
			}
			if (pExempt == null) {
				plugin().log(Level.INFO, " Unable to exempt {0}, player not found", exempt);
			} else {
				plugin().log(Level.INFO, " Adding limits exemption for {0} as {1}", exempt, pExempt);
				this.exemptFromLimits.add(pExempt);
			}
		}
	}

	/**
	 * Wireup for enabling setting your spawn during the day.
	 *
	 * @author Amelorate
	 */
	private void wireupDaytimeBed(ConfigurationSection config) {
		if (config == null) {
			this.daytimeBedEnabled = false;
			return;
		}

		this.daytimeBedEnabled = config.getBoolean("enabled", false);
		this.daytimeBedSpawnSetMessage = ChatColor.translateAlternateColorCodes('&',
				config.getString("spawnSetMessage", ChatColor.GRAY + "Your spawn has been set."));
	}

	/**
	 * @return true / false if chunk limits are on
	 */
	public boolean areChunkLimitsEnabled() {
		return this.chunkLimitsEnabled;
	}

	/**
	 * Gets the chunk limit for a specified material.
	 * Returns null if no limit is set.
	 *
	 * @param mat the Material to return a limit on, if set.
	 * @return the max # per chunk, or null if no limit set.
	 */
	public Integer getChunkLimit(Material mat) {
		return this.chunkLimitsEnabled ? this.blockEntityLimits.get(mat) : null;
	}

	/**
	 * Returns false if the supplied UUID is exempt from limits.
	 *
	 * @param player the UUID to check
	 * @returns false it the player is exempt, true otherwise.
	 */
	public boolean applyChunkLimits(UUID player) {
		return this.chunkLimitsEnabled ? !this.exemptFromLimits.contains(player) : false;
	}

	/**
	 * @return the generic limit message. TODO: Replace with customized message tailored per player.
	 */
	public String getChunkLimitsExceededMessage() {
		return this.chunkLimitsExceededMessage;
	}

	/**
	 * @return The message that is sent to the player if they right click on a bed and set their spawn.
	 * Empty string if there should not be a message, and null if daytime beds are disabled.
	 */
	public String getDaytimeBedSpawnSetMessage() {
		return daytimeBedEnabled ? daytimeBedSpawnSetMessage : null;
	}

	/**
	 * @return If setting your spawn with a bed during the daytime is enabled.
	 */
	public boolean areDaytimeBedsEnabled() {
		return daytimeBedEnabled;
	}

	/**
	 * @return If one to one nether is enabled.
	 */
	public boolean isOneToOneNether() {
		return oneToOneNether;
	}

	/**
	 * @return If return portals are enabled.
	 */
	public boolean isReturnNetherPortal() {
		return returnNetherPortal;
	}

	public boolean allowNetherTravel() {
		return allowNetherTravel;
	}

	public boolean isChestedMinecartInventories() {
		return chestedMinecartInventories;
	}

	public boolean isHopperMinecartInventories() {
		return hopperMinecartInventories;
	}

	public boolean isEnderChestInventories() {
		return enderChestInventories;
	}

	public boolean stopTrapHorses() {
		return stopTrapHorses;
	}

	public boolean killTrapHorses() {
		return killTrapHorses;
	}

	public boolean canChangeSpawnerType() {
		return changeSpawnerType;
	}

	public boolean allowVillagerTrading() {
		return allowVillagerTrading;
	}

	public boolean isEnderGrief() {
		return enderGrief;
	}

	public boolean isWitherGrief() {
		return witherGrief;
	}

	public boolean isPreventFallingThroughBedrock() {
		return preventFallingThroughBedrock;
	}

	public boolean canPlace(Material mat) {
		return !noPlace.contains(mat);
	}

}