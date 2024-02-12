package vg.civcraft.mc.civmodcore.players.settings.impl;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;

public class StringSetting extends PlayerSetting<String> {

	public StringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(plugin, defaultValue, name, identifier, gui, description, true);
	}

	public StringSetting(JavaPlugin plugin, String defaultValue, String identifier) {
		super(plugin, defaultValue, identifier, identifier, new ItemStack(Material.STONE), "", false);
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
