package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public class BooleanSetting extends PlayerSetting<Boolean> {

	public BooleanSetting(JavaPlugin owningPlugin, Boolean defaultValue, String name, String identifier, ItemStack gui,
			String description) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
	}

	@Override
	protected Boolean deserialize(String serial) {
		return Boolean.valueOf(serial);
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ItemStack item;
		if (getValue(player)) {
			item = new ItemStack(Material.LIME_DYE);
		} else {
			item = new ItemStack(Material.ROSE_RED);
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
	protected String serialize(Boolean value) {
		return String.valueOf(value);
	}

	public void toggleValue(UUID uuid) {
		setValue(uuid, !getValue(uuid));
	}

	@Override
	protected String toText(Boolean value) {
		return String.valueOf(value);
	}
}
