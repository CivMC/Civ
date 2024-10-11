package com.programmerdan.minecraft.simpleadminhacks.configs;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.BuildLimit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public final class BuildLimitsConfig extends SimpleHackConfig {
	private boolean enabled;
	private BuildLimit[] buildLimits;

	public BuildLimitsConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	public BuildLimitsConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
		this.enabled = config.getBoolean("enabled");

		if (this.enabled) {
			List<?> rawList = config.getList("limits");
			if (rawList != null && rawList.size() > 0) {
				try {
					this.buildLimits = rawList.toArray(new BuildLimit[rawList.size()]);
					plugin().getLogger().info("buildlimits enabled");
				} catch(ArrayStoreException ase) {
					plugin().getLogger().warning("buildlimits was enabled, but is invalid");
					ase.printStackTrace();
				}
			} else {
				plugin().getLogger().warning("buildlimits was enabled, but is missing or empty");
			}
		} else {
			plugin().getLogger().info("buildlimits disabled");
		}
	}

	public boolean isEnabled() {
		return this.enabled;
	}
	public BuildLimit[] getBuildLimits() {
		return this.buildLimits;
	}
}
