package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class LongSetting extends PlayerSetting<Long> {

	public LongSetting(JavaPlugin owningPlugin, Long defaultValue, String name, String identifier, ItemStack gui, String description) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, true);
	}
	
	public LongSetting(JavaPlugin owningPlugin, Long defaultValue, String name, String identifier) {
		super(owningPlugin, defaultValue, name, identifier, new ItemStack(Material.STONE), null, false);
	}

	@Override
	public Long deserialize(String serial) {
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
	public String serialize(Long value) {
		return String.valueOf(value);
	}

	@Override
	public String toText(Long value) {
		return String.valueOf(value);
	}

}
