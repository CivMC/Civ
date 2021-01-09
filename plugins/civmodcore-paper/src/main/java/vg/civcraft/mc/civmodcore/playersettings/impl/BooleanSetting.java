package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public class BooleanSetting extends PlayerSetting<Boolean> {

	public BooleanSetting(JavaPlugin owningPlugin, Boolean defaultValue, String name, String identifier,
						  String description) {
		super(owningPlugin, defaultValue, name, identifier, new ItemStack(Material.STONE), description, true);
	}

	@Override
	public Boolean deserialize(String serial) {
		switch (serial.toLowerCase()) {
			case "1":
			case "true":
			case "t":
			case "y":
			case "yes":
				return true;
			case "0":
			case "false":
			case "f":
			case "n":
			case "no":
				return false;
			case "null":
				return null;
		}
		throw new IllegalArgumentException(serial + " is not a valid boolean value");
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack item;
		if (getValue(player)) {
			item = new ItemStack(Material.LIME_DYE);
		} else {
			item = new ItemStack(Material.RED_DYE);
		}
		applyInfoToItemStack(item, player);
		return item;
	}

	@Override
	public void handleMenuClick(Player player, MenuSection menu) {
		setValue(player.getUniqueId(), !getValue(player.getUniqueId()));
		menu.showScreen(player);
	}

	@Override
	public String serialize(Boolean value) {
		return String.valueOf(value);
	}

	public void toggleValue(UUID uuid) {
		setValue(uuid, !getValue(uuid));
	}

	@Override
	public String toText(Boolean value) {
		return String.valueOf(value);
	}

	@Override
	public boolean isValidValue(String input) {
		switch (input.toLowerCase()) {
			case "1":
			case "true":
			case "t":
			case "y":
			case "yes":
			case "0":
			case "false":
			case "f":
			case "n":
			case "no":
			case "null":
				return true;
			default:
				return false;
		}
	}

}
