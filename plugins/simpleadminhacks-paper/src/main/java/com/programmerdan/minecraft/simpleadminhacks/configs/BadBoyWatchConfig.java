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

	private int trackingDepth = 8;
	private boolean clearOnMatch = true;
	private int minDepthToMatch = 2; // default; block + something else.
	private Set<Material> watchedMaterials = null;
	
	private static final List<Material> defaultWatched = Arrays.asList(
			Material.CHEST,
			Material.JUKEBOX,
			Material.NOTE_BLOCK,
			Material.BEACON,
			Material.ENCHANTMENT_TABLE,
			Material.ENDER_CHEST,
			Material.FURNACE,
			Material.SPONGE);
	
	public BadBoyWatchConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.trackingDepth = config.getInt("trackingDepth", this.trackingDepth);
		this.clearOnMatch = config.getBoolean("clearOnMatch", this.clearOnMatch);
		this.minDepthToMatch = config.getInt("minDepthToMatch", this.minDepthToMatch);
		@SuppressWarnings("unchecked")
		List<String> watch = (List<String>) config.getList("watchedMaterials");
		this.watchedMaterials = new HashSet<Material>();
		if (watch == null) {
			this.watchedMaterials.addAll(defaultWatched);
		} else {
			for (String mat : watch) {
				try {
					this.watchedMaterials.add(Material.matchMaterial(mat));
				} catch (Exception e) {
					plugin().getLogger().log(Level.WARNING, "Invalid material {0} listed", mat);
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
