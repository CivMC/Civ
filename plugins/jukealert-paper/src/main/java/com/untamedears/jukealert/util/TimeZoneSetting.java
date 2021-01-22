package com.untamedears.jukealert.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.playersettings.impl.StringSetting;

public class TimeZoneSetting extends StringSetting {

	public TimeZoneSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(plugin, defaultValue, name, identifier, gui, description);
	}
	
	@Override
	public boolean isValidValue(String input) {
		if (input == null) {
			return false;
		}
		//TimeZone.getTimeZone(input);
		return true;
	}

}
