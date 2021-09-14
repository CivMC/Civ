package com.untamedears.jukealert.model.appender.config;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.bukkit.configuration.ConfigurationSection;
import vg.civcraft.mc.civmodcore.config.ConfigHelper;
import vg.civcraft.mc.civmodcore.util.ConfigParsing;

public class LimitedActionTriggerConfig implements AppenderConfig {

	private final Set<String> identifier;
	private final boolean acceptAll;
	private final long actionLifespan;
	private final int hardCap;

	public LimitedActionTriggerConfig(final ConfigurationSection config) {
		this.identifier = new HashSet<>();
		if (config.isList("trigger")) {
			final List<String> triggers = ConfigHelper.getStringList(config, "trigger");
			final LoggedActionFactory actionFactory = JukeAlert.getInstance().getLoggedActionFactory();
			for (String trigger : triggers) {
				trigger = trigger.toUpperCase().trim();
				if (actionFactory.getInternalID(trigger) != -1) {
					this.identifier.add(trigger);
				}
			}
		}
		this.acceptAll = config.getBoolean("acceptAll", false);
		this.actionLifespan = ConfigParsing.parseTime(config.getString("lifeTime", "4 weeks"), TimeUnit.MILLISECONDS);
		this.hardCap = config.getInt("hardCap", 100_000);
	}

	public boolean isTrigger(final String actionType) {
		return this.acceptAll || this.identifier.contains(actionType);
	}

	public long getActionLifespan() {
		return this.actionLifespan;
	}

	public int getHardCap() {
		return this.hardCap;
	}

}
