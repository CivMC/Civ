package com.programmerdan.minecraft.simpleadminhacks.framework;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

public abstract class BasicHack extends SimpleHack<BasicHackConfig> implements Listener {

	public BasicHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	// REMEMBER TO CALL SUPER
	@Override
	public void onEnable() {
		plugin().registerListener(this);
	}

	// REMEMBER TO CALL SUPER
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
	}

	// YOU DON'T HAVE TO COPY THIS! It's inherited.
	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
