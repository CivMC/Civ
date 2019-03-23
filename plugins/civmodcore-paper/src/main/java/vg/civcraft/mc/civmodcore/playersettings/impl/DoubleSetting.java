package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.text.DecimalFormat;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.StringInputSetting;

public class DoubleSetting extends StringInputSetting<Double> {

	private DecimalFormat formatter;

	public DoubleSetting(JavaPlugin owningPlugin, Double defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		this(owningPlugin, defaultValue, name, identifier, gui, description, 2);
	}

	public DoubleSetting(JavaPlugin owningPlugin, Double defaultValue, String name, String identifier, ItemStack gui,
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
	protected Double deserialize(String serial) {
		return Double.parseDouble(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			Double.parseDouble(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	protected String serialize(Double value) {
		return String.valueOf(value);
	}

	@Override
	protected String toText(Double value) {
		return formatter.format(value);
	}
}
