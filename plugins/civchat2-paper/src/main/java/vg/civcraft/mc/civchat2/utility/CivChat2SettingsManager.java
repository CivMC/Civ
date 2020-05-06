package vg.civcraft.mc.civchat2.utility;

import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

import java.util.UUID;

public class CivChat2SettingsManager {

	private BooleanSetting showJoins;
	private BooleanSetting showLeaves;

	public CivChat2SettingsManager() {
		initSettings();
	}

	private void initSettings(){
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("CivChat","All options related to CivChat.");

		showJoins = new BooleanSetting(CivChat2.getInstance(), true, "Show Player Joins", "showJoins", "Hides player join messaged if disabled.");
		PlayerSettingAPI.registerSetting(showJoins, menu);

		showLeaves = new BooleanSetting(CivChat2.getInstance(), true, "Show Players Leaving", "showLeaves", "Hides player leaving messages if disabled.");
		PlayerSettingAPI.registerSetting(showLeaves, menu);
	}

	public boolean getShowJoins(UUID uuid) {
		return showJoins.getValue(uuid);
	}

	public Boolean getShowLeaves(UUID uuid) {
		return showLeaves.getValue(uuid);
	}
}
