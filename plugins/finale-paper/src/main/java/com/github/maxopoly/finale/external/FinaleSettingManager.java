package com.github.maxopoly.finale.external;

import java.util.UUID;

import com.github.maxopoly.finale.Finale;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;

public class FinaleSettingManager {

	private BooleanSetting vanillaPearlCooldown;
	private BooleanSetting actionBarPearlCooldown;
	private BooleanSetting sideBarPearlCooldown;
	private BooleanSetting showArmor;
	private BooleanSetting showTool;
	private BooleanSetting showPotionEffects;
	private BooleanSetting permanentNightVision;

	public FinaleSettingManager() {
		initSettings();
	}

	public void initSettings() {
		Finale plugin = Finale.getPlugin();
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Combat",
				"Combat and Finale related settings");
		
		permanentNightVision = new BooleanSetting(plugin, false, "Enable GammaBright",
				"finaleGammaBright",
				"Do you want permanent night vision");
		PlayerSettingAPI.registerSetting(permanentNightVision, menu);
		
		vanillaPearlCooldown = new BooleanSetting(plugin, false, "Use vanilla pearl cooldown",
				"finaleVanillaPearlCooldown",
				"Should pearl cooldown be shown as an overlay on the item, the way it is done in vanilla");
		PlayerSettingAPI.registerSetting(vanillaPearlCooldown, menu);
		
		actionBarPearlCooldown = new BooleanSetting(plugin, true, "Show pearl cooldown on action bar",
				"finaleActionBarPearlCooldown",
				"Should pearl cooldown be shown on the action bar at the bottom of your screen");
		PlayerSettingAPI.registerSetting(actionBarPearlCooldown, menu);
		
		sideBarPearlCooldown = new BooleanSetting(plugin, false, "Show pearl cooldown in side bar",
				"finaleSideBarPearlCooldown",
				"Should pearl cooldown be shown on the sidebar");
		PlayerSettingAPI.registerSetting(sideBarPearlCooldown, menu);
		
		showArmor = new BooleanSetting(Finale.getPlugin(), true, "Show armor durabiltiy", "finaleArmorDurabilitySidebar","Should the durability of your worn armor be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showArmor, menu);
		
		showTool = new BooleanSetting(Finale.getPlugin(), true, "Show tool durabiltiy", "finaleToolDurabilitySidebar","Should the durability of your held tool be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showTool, menu);
		
		showPotionEffects = new BooleanSetting(Finale.getPlugin(), true, "Show active potion effects", "finaleShowPotionsSidebar","Should active potion effects be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showPotionEffects, menu);
	}
	
	public boolean setVanillaPearlCooldown(UUID uuid) {
		return vanillaPearlCooldown.getValue(uuid);
	}
	
	public BooleanSetting getGammaBrightSetting() {
		return permanentNightVision;
	}
	
	public BooleanSetting getArmorSetting() {
		return showArmor;
	}
	
	public BooleanSetting getToolSetting() {
		return showTool;
	}
	
	public BooleanSetting getPotionSetting() {
		return showPotionEffects;
	}
	
	public boolean sideBarPearlCooldown(UUID uuid) {
		return sideBarPearlCooldown.getValue(uuid);
	}
	
	public boolean actionBarPearlCooldown(UUID uuid) {
		return actionBarPearlCooldown.getValue(uuid);
	}
	
	public boolean showArmorDurability(UUID uuid) {
		return showArmor.getValue(uuid);
	}
	
	public boolean showToolDurability(UUID uuid) {
		return showTool.getValue(uuid);
	}
	
	public boolean showPotionEffects(UUID uuid) {
		return showPotionEffects.getValue(uuid);
	}

}
