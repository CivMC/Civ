package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.StringInputSetting;

public class LongSetting extends StringInputSetting<Long> {

	public LongSetting(JavaPlugin owningPlugin, Long defaultValue, String name, String identifier, ItemStack gui, String description) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
	}

	@Override
	protected Long deserialize(String serial) {
		return Long.parseLong(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			Long.parseLong(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected String serialize(Long value) {
		return String.valueOf(value);
	}

	@Override
	protected String toText(Long value) {
		return String.valueOf(value);
	}
}
