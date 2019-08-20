package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Configures this hack, determines which blocks to watch and the like.
 * 
 * @author ProgrammerDan
 */
public class BadBoyWatchConfig extends SimpleHackConfig {

	private int trackingDepth;
	private boolean clearOnMatch;
	private int minDepthToMatch;
	private Set<Material> watchedMaterials;

	private static final List<Material> defaultWatched = Arrays.asList(
			Material.CHEST,
			Material.JUKEBOX,
			Material.NOTE_BLOCK,
			Material.BEACON,
			Material.ENCHANTING_TABLE,
			Material.ENDER_CHEST,
			Material.FURNACE,
			Material.SPONGE);

	public BadBoyWatchConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.trackingDepth = config.getInt("trackingDepth", 8); // keep up to 8 breaks
		plugin().log(Level.INFO, "  Tracking Depth: {0}", this.trackingDepth);
		this.clearOnMatch = config.getBoolean("clearOnMatch", true); // once you print out don't keep old records
		plugin().log(Level.INFO, "  Clear On Match: {0}", this.clearOnMatch);
		this.minDepthToMatch = config.getInt("minDepthToMatch", 2); // this block + 1
		plugin().log(Level.INFO, "  Minimum Breakpath Depth To Match: {0}", this.minDepthToMatch);
		@SuppressWarnings("unchecked")
		List<String> watch = (List<String>) config.getList("watchedMaterials");
		this.watchedMaterials = new HashSet<>();
		if (watch == null || watch.isEmpty()) {
			this.watchedMaterials.addAll(defaultWatched);
			plugin().log(Level.INFO, "  Adding default watch set");
		} else {
			for (String mat : watch) {
				try {
					Material nmat = Material.matchMaterial(mat);
					this.watchedMaterials.add(nmat);
					plugin().log(Level.INFO, "  Watching material {0}", nmat);
				} catch (Exception e) {
					plugin().log(Level.WARNING, "  Invalid material {0} listed", mat);
				}
			}
		}
	}

	public int getTrackingDepth() {
		return trackingDepth;
	}

	public boolean isClearOnMatch() {
		return clearOnMatch;
	}

	public int getMinDepthToMatch() {
		return minDepthToMatch;
	}

	public Set<Material> getWatchedMaterials() {
		return watchedMaterials;
	}

}
