package com.programmerdan.minecraft.simpleadminhacks.configs;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHackConfig;

/**
 * Define an "into" list of Strings where the Strings are event classpaths.
 * Can monitor not only bukkit events but also plugin / custom events; just give the
 * valid classpath.
 * 
 * This bypasses the "safe" bukkit way of registering listeners for events. So it
 * also bypasses Timings.
 * 
 * This is the only warning I'll give. Be careful with this and prefer to leave it off.
 * 
 * Wireups only happen on restart, I'm dubious about injecting live for now.
 * 
 * @author ProgrammerDan
 *
 */
public class InsightConfig extends SimpleHackConfig {
	private List<String> insightOn;

	public InsightConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		if (config.contains("into")) {
			insightOn = config.getStringList("into");
		} else {
			insightOn = null;
		}
	}

	public List<String> getInsightOn() {
		return insightOn;
	}
}
