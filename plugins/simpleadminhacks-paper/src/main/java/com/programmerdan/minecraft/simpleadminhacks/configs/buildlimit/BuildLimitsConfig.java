package com.programmerdan.minecraft.simpleadminhacks.configs.buildlimit;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.SimpleHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.BuildLimit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public final class BuildLimitsConfig extends SimpleHackConfig {
	private BuildLimit[] buildLimits;

	public BuildLimitsConfig(SimpleAdminHacks plugin, ConfigurationSection base) {
		super(plugin, base);
	}
	public BuildLimitsConfig(ConfigurationSection base) {
		super(SimpleAdminHacks.instance(), base);
	}

	@Override
	protected void wireup(ConfigurationSection config) {
        List<?> rawList = config.getList("limits");
        if (rawList != null && rawList.size() > 0) {
            this.buildLimits = rawList.toArray(new BuildLimit[rawList.size()]);
        }
	}
	public BuildLimit[] getBuildLimits() {
		return this.buildLimits;
	}
}
