package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;

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

	public GameTuningConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		ConfigurationSection chunkLimits = config.getConfigurationSection("chunkLimits");
		wireupChunkLimits(chunkLimits);

		ConfigurationSection daytimeBed = config.getConfigurationSection("daytimeBed");
		wireupDaytimeBed(daytimeBed);

		/* Add additional tuning config grabs here. */
	}

	/**
	 * Wireup for Chunk Limits configuration
	 * 
	 * @author ProgrammerDan
	 *
	 */ 
	private void wireupChunkLimits(ConfigurationSection config) {
		this.blockEntityLimits = new HashMap<Material, Integer>();
		this.exemptFromLimits = new HashSet<UUID>();
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
}
