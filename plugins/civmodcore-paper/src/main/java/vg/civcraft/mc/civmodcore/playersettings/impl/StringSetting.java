package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.StringInputSetting;

public class StringSetting extends StringInputSetting<String> {

	public StringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(plugin, defaultValue, name, identifier, gui, description);
	}

	@Override
	protected String deserialize(String serial) {
		return serial;
	}

	@Override
	public boolean isValidValue(String input) {
		return input != null;
	}

	@Override
	protected String serialize(String value) {
		return value;
	}

	@Override
	protected String toText(String value) {
		return value;
	}

}
