package com.untamedears.jukealert.util;

import com.untamedears.jukealert.JukeAlert;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.BoundedIntegerSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting.DisplayLocation;
import vg.civcraft.mc.civmodcore.playersettings.impl.collection.SetSetting;

public class JASettingsManager {

	private BoundedIntegerSetting jaInfoSize;
	private BoundedIntegerSetting jaListSize;
	private BooleanSetting ignoreAllAlerts;
	private DisplayLocationSetting showNearbySnitchCount;
	private SetSetting<String> ignoredGroupAlerts;
	private BooleanSetting showDirections;
	private BooleanSetting monoColorAlerts;

	public JASettingsManager() {
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("JukeAlert",
				"JukeAlert and snitch related settings", new ItemStack(Material.JUKEBOX));
		
		jaInfoSize = new BoundedIntegerSetting(JukeAlert.getInstance(), 8, "/jainfo items per page", "jaInfoListSize",
				new ItemStack(Material.JUKEBOX), "How many entries should shown per page of /jainfo", true, 1, 50);
		PlayerSettingAPI.registerSetting(jaInfoSize, menu);
		
		jaListSize = new BoundedIntegerSetting(JukeAlert.getInstance(), 8, "/jalist items per page", "jaListListSize",
				new ItemStack(Material.NOTE_BLOCK), "How many entries should shown per page of /jalist", true, 1, 50);
		PlayerSettingAPI.registerSetting(jaListSize, menu);
		
		ignoreAllAlerts = new BooleanSetting(JukeAlert.getInstance(), false, "Ignore all snitch alerts",
				"jaIgnoreAllSnitchAlerts", "Mutes all snitch notifications if enabled");
		PlayerSettingAPI.registerSetting(ignoreAllAlerts, menu);
		
		showNearbySnitchCount = new DisplayLocationSetting(JukeAlert.getInstance(), DisplayLocation.SIDEBAR, "Show nearby snitch count",
				"jaShowNearbySnitchCount", new ItemStack(Material.JUKEBOX),"Nearby snitch count");
		PlayerSettingAPI.registerSetting(showNearbySnitchCount, menu);
		
		ignoredGroupAlerts = new SetSetting<>(JukeAlert.getInstance(), "Ignored group alerts", "jaIgnoredSnitchGroups",
				new ItemStack(Material.BELL), "Groups you have muted, meaning you won't receive snitch alerts from them",
				String.class);
		PlayerSettingAPI.registerSetting(ignoredGroupAlerts, menu);

		showDirections = new BooleanSetting(JukeAlert.getInstance(), true, "Show directions to snitch on hit", "jaShowDirections",
				"Shows the direction and distance to the snitch when triggered.");
		PlayerSettingAPI.registerSetting(showDirections, menu);
		
		monoColorAlerts = new BooleanSetting(JukeAlert.getInstance(), false, "Show snitch alerts in a single color", "jaMonoColorNotifications",
				"Shows snitch notifications in only a single color");
		PlayerSettingAPI.registerSetting(monoColorAlerts, menu);
	}

	public SetSetting<String> getIgnoredGroupAlerts() {
		return ignoredGroupAlerts;
	}

	public boolean doesIgnoreAlert(String groupName, UUID uuid) {
		return ignoredGroupAlerts.contains(uuid, groupName);
	}
	
	public DisplayLocationSetting getNearbySnitchLocation() {
		return showNearbySnitchCount;
	}

	public boolean doesIgnoreAllAlerts(UUID uuid) {
		return ignoreAllAlerts.getValue(uuid);
	}
	
	public boolean monocolorAlerts(UUID uuid) {
		return monoColorAlerts.getValue(uuid);
	}

	public int getJaListLength(UUID uuid) {
		return jaListSize.getValue(uuid);
	}

	public int getJaInfoLength(UUID uuid) {
		return jaInfoSize.getValue(uuid);
	}

	public boolean shouldShowDirections(UUID uuid) {
		return showDirections.getValue(uuid);
	}

}
