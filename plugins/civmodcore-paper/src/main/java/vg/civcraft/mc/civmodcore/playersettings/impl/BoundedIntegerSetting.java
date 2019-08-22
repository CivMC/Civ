package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.api.ItemAPI;

public class BoundedIntegerSetting extends IntegerSetting {

	private int lowerBound;
	private int upperBound;

	public BoundedIntegerSetting(JavaPlugin owningPlugin, int defaultValue, String name, String identifier,
			ItemStack gui, String description, boolean showAmountInGui, int lowerBound, int upperBound) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, showAmountInGui);
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
		if (lowerBound > upperBound) {
			throw new IllegalArgumentException("Lower bound can not be bigger than upper bound");
		}
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			int number = Integer.parseInt(input);
			return number >= lowerBound && number <= upperBound;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack stack = super.getGuiRepresentation(player);
		ItemAPI.addLore(stack,
				String.format("%sMust be at least %d and at maximum %d", ChatColor.GOLD, lowerBound, upperBound));
		return stack;
	}

}
