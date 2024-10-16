package com.programmerdan.minecraft.simpleadminhacks.framework;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Use this when your hack either doesn't have any config values, or you're loading all your values though
 * {@link AutoLoad}. If you want to extend this, extend {@link SimpleHackConfig} directly.
 */
public final class BasicHackConfig extends SimpleHackConfig {

	public BasicHackConfig(final SimpleAdminHacks plugin, final ConfigurationSection base) {
		super(plugin, base);
	}

	@Override
	protected void wireup(final ConfigurationSection config) { }

}
