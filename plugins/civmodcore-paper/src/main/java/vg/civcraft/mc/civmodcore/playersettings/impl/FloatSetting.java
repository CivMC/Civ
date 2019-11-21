package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.text.DecimalFormat;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class FloatSetting extends PlayerSetting<Float> {

	private DecimalFormat formatter;

	public FloatSetting(JavaPlugin owningPlugin, Float defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		this(owningPlugin, defaultValue, name, identifier, gui, description, 2);
	}

	public FloatSetting(JavaPlugin owningPlugin, Float defaultValue, String name, String identifier, ItemStack gui,
			String description, int decimalPointsVisible) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
		StringBuilder sb = new StringBuilder();
		sb.append("0.");
		for (int i = 0; i < decimalPointsVisible; i++) {
			sb.append("#");
		}
		formatter = new DecimalFormat(sb.toString());
	}

	@Override
	public Float deserialize(String serial) {
		return Float.parseFloat(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			Float.parseFloat(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String serialize(Float value) {
		return String.valueOf(value);
	}

	@Override
	public String toText(Float value) {
		return formatter.format(value);
	}

}
