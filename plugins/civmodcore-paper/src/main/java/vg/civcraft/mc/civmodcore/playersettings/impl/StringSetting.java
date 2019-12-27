package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class StringSetting extends PlayerSetting<String> {

	public StringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(plugin, defaultValue, name, identifier, gui, description);
	}

	@Override
	public String deserialize(String serial) {
		return serial;
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null;
	}

	@Override
	public String serialize(String value) {
		return value;
	}

	@Override
	public String toText(String value) {
		return value;
	}

}
