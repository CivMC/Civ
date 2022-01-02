package com.programmerdan.minecraft.simpleadminhacks;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class SimpleAdminHacksSettingManager {

	private MenuSection mainMenu;

	public SimpleAdminHacksSettingManager() {
		this.mainMenu = PlayerSettingAPI.getMainMenu().createMenuSection("SimpleAdminHacks", "Config values for all small tweaks from SAH", new ItemStack(
				Material.CAULDRON));
	}

	/**
	 * Returns the Menu Section used to bundle together all SAH /config tweaks
	 * @return SAH /config Menu
	 */
	public MenuSection getMainMenu() {
		return mainMenu;
	}
}
