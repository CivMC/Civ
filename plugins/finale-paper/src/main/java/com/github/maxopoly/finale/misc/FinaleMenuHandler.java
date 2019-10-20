package com.github.maxopoly.finale.misc;

import java.util.UUID;

import com.github.maxopoly.finale.Finale;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

public class FinaleMenuHandler {

	private BooleanSetting showPearlOnSidebar;
	private BooleanSetting showPearlOnActionBar;

	public FinaleMenuHandler() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Combat", "Combat/Finale related settings");
		Finale plugin = Finale.getPlugin();
		showPearlOnSidebar = new BooleanSetting(plugin, true, "Show pearl cooldown on sidebar", "pearlsSidebar",
				"Should the ender pearl cooldown be shown on the score board");
		PlayerSettingAPI.registerSetting(showPearlOnSidebar, menu);
		showPearlOnActionBar = new BooleanSetting(plugin, false, "Show pearl cooldown on action bar", "pearlsActionbar",
				"Should the ender pearl cooldown be shown on the action bar");
		PlayerSettingAPI.registerSetting(showPearlOnActionBar, menu);
	}
	
	public boolean showPearlOnSidebar(UUID uuid) {
		return showPearlOnSidebar.getValue(uuid);
	}
	
	public boolean showPearlOnActionbar(UUID uuid) {
		return showPearlOnActionBar.getValue(uuid);
	}

}
