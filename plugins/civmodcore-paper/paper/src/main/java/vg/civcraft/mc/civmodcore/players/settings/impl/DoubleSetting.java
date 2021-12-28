package vg.civcraft.mc.civmodcore.players.settings.impl;

import java.text.DecimalFormat;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSetting;

public class DoubleSetting extends PlayerSetting<Double> {

	private DecimalFormat formatter;

	public DoubleSetting(JavaPlugin owningPlugin, Double defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		this(owningPlugin, defaultValue, name, identifier, gui, description, 2);
	}

	public DoubleSetting(JavaPlugin owningPlugin, Double defaultValue, String name, String identifier, ItemStack gui,
			String description, int decimalPointsVisible) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, true);
		StringBuilder sb = new StringBuilder();
		sb.append("0.");
		for (int i = 0; i < decimalPointsVisible; i++) {
			sb.append("#");
		}
		formatter = new DecimalFormat(sb.toString());
	}

	@Override
	public Double deserialize(String serial) {
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
	public String serialize(Double value) {
		return String.valueOf(value);
	}

	@Override
	public String toText(Double value) {
		return formatter.format(value);
	}

}
