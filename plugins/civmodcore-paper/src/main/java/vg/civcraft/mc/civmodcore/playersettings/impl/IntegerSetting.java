package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class IntegerSetting extends PlayerSetting<Integer> {

	private boolean showAmountInGui;

	public IntegerSetting(JavaPlugin owningPlugin, Integer defaultValue, String name, String identifier, ItemStack gui,
			String description, boolean showAmountInGui) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, true);
		this.showAmountInGui = showAmountInGui;
	}

	@Override
	public Integer deserialize(String serial) {
		return Integer.parseInt(serial);
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack stack = super.getGuiRepresentation(player);
		if (showAmountInGui) {
			stack.setAmount(getValue(player));
		}
		return stack;
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public String serialize(Integer value) {
		return String.valueOf(value);
	}

	@Override
	public String toText(Integer value) {
		return String.valueOf(value);
	}

}
