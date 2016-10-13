package com.programmerdan.minecraft.simpleadminhacks.configs;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Holds configurations for the GameTuning module.
 *
 * @author ProgrammerDan
 */
public class GameTuningConfig extends SimpleHackConfig {

	private Map<Material, Integer> blockEntityLimits;
	private Set<UUID> exemptFromLimits;
	private boolean limitsPreventExploits;

	public GameTuningConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.blockEntityLimits = new HashMap<Material, Integer>();
		// for each "chunkLimits.tileEntities: " entry, record limit.
		this.exemptFromLimits = new HashSet<UUID>();
		// for each "chunkLimits.exempt: " entry, record limit.
		this.limitsPreventExploits = config.getBoolean("chunkLimits.preventExploits", true);
		// Prevent piston movement, etc.

	}
}
