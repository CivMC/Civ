package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.text.DecimalFormat;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;

public class DecimalFormatSetting extends PlayerSetting<DecimalFormat> {

	private double exampleValue;

	public DecimalFormatSetting(JavaPlugin owningPlugin, DecimalFormat defaultValue, String name, String identifier,
			ItemStack gui, String description, double exampleValue) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, true);
		this.exampleValue = exampleValue;
	}

	@Override
	public DecimalFormat deserialize(String serial) {
		return new DecimalFormat(serial);
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			new DecimalFormat(input);
			return true;
		} catch (NullPointerException | IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String serialize(DecimalFormat value) {
		return value.toPattern();
	}

	@Override
	public String toText(DecimalFormat value) {
		return value.toPattern();
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack item = super.getGuiRepresentation(player);
		ItemAPI.addLore(item, ChatColor.GOLD + "Example: " + ChatColor.RESET + getValue(player).format(exampleValue));
		return item;
	}

}
