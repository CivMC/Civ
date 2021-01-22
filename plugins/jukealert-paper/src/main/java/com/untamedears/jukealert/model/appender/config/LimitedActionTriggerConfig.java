package com.untamedears.jukealert.model.appender.config;

import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.actions.LoggedActionFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;

public class LimitedActionTriggerConfig implements AppenderConfig {

	private Set<String> identifier;
	private boolean acceptAll;

	public LimitedActionTriggerConfig(ConfigurationSection config) {
		this.identifier = new HashSet<>();
		if (config.isList("trigger")) {
			List<String> triggers = config.getStringList("trigger");
			LoggedActionFactory fac = JukeAlert.getInstance().getLoggedActionFactory();
			for (String trigger : triggers) {
				trigger = trigger.toUpperCase().trim();
				if (fac.getInternalID(trigger) != -1) {
					identifier.add(trigger);
				}
			}
		}
		acceptAll = config.getBoolean("acceptAll", false);
	}

	public boolean isTrigger(String actionType) {
		if (acceptAll) {
			return true;
		}
		return identifier.contains(actionType);
	}

}
