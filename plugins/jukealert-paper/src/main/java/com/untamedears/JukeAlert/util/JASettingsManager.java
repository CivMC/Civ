package com.untamedears.JukeAlert.util;

import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.JukeAlert;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BoundedIntegerSetting;

public class JASettingsManager {

	private BoundedIntegerSetting jaInfoSize;
	private BoundedIntegerSetting jaListSize;

	public JASettingsManager() {
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("JukeAlert",
				"JukeAlert and snitch related settings");
		jaInfoSize = new BoundedIntegerSetting(JukeAlert.getInstance(), 8, "/jainfo items per page", "jaInfoListSize",
				new ItemStack(Material.JUKEBOX), "How many entries should shown per page of /jainfo", true, 1, 50);
		jaListSize = new BoundedIntegerSetting(JukeAlert.getInstance(), 8, "/jalist items per page", "jaListListSize",
				new ItemStack(Material.NOTE_BLOCK), "How many entries should shown per page of /jalist", true, 1, 50);
		PlayerSettingAPI.registerSetting(jaInfoSize, menu);
		PlayerSettingAPI.registerSetting(jaListSize, menu);
	}

	public int getJaListLength(UUID uuid) {
		return jaListSize.getValue(uuid);
	}

	public int getJaInfoLength(UUID uuid) {
		return jaInfoSize.getValue(uuid);
	}

}
