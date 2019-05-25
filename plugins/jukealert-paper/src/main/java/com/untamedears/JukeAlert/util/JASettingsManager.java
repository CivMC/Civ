package com.untamedears.JukeAlert.util;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public class JASettingsManager {
	
	public JASettingsManager() {
		initSettings();
	}
	
	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("JukeAlert",
				"JukeAlert and snitch related settings");
	}

}
