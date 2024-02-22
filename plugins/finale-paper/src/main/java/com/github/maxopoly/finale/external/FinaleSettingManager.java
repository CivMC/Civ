package com.github.maxopoly.finale.external;

import com.github.maxopoly.finale.Finale;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.players.settings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.BoundedIntegerSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.DisplayLocationSetting;
import vg.civcraft.mc.civmodcore.players.settings.impl.IntegerSetting;

public class FinaleSettingManager {

	private BooleanSetting vanillaPearlCooldown;
	private BooleanSetting vanillaItemCooldown;
	private BooleanSetting actionBarItemCooldown;
	private BooleanSetting actionBarPearlCooldown;
	private BooleanSetting sideBarPearlCooldown;
	private BooleanSetting sideBarItemCooldown;
	private BooleanSetting showArmor;
	private BooleanSetting showTool;
	private BooleanSetting showPotionEffects;
	private BooleanSetting permanentNightVision;
	private BooleanSetting toolProtection;
	private DisplayLocationSetting coordsLocation;
	private IntegerSetting toolProtectionThreshhold;

	public FinaleSettingManager() {
		initSettings();
	}

	public void initSettings() {
		Finale plugin = Finale.getPlugin();
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Combat",
				"Combat and Finale related settings", new ItemStack(Material.DIAMOND_SWORD));

		permanentNightVision = new BooleanSetting(plugin, false, "Enable GammaBright",
				"finaleGammaBright",
				"Do you want permanent night vision");
		PlayerSettingAPI.registerSetting(permanentNightVision, menu);

		vanillaPearlCooldown = new BooleanSetting(plugin, false, "Use vanilla pearl cooldown",
				"finaleVanillaPearlCooldown",
				"Should pearl cooldown be shown as an overlay on the item, the way it is done in vanilla");
		PlayerSettingAPI.registerSetting(vanillaPearlCooldown, menu);

		vanillaItemCooldown = new BooleanSetting(plugin, false, "Use vanilla item cooldown",
			"finaleVanillaItemCooldown",
			"Should items cooldown be shown as an overlay on the item, the way it is done in vanilla");
		PlayerSettingAPI.registerSetting(vanillaItemCooldown, menu);

		actionBarPearlCooldown = new BooleanSetting(plugin, true, "Show pearl cooldown on action bar",
				"finaleActionBarPearlCooldown",
				"Should pearl cooldown be shown on the action bar at the bottom of your screen");
		PlayerSettingAPI.registerSetting(actionBarPearlCooldown, menu);

		actionBarItemCooldown = new BooleanSetting(plugin, true, "Show item cooldown on action bar",
			"finaleActionBarItemCooldown",
			"Should items cooldown be shown on the action bar at the bottom of your screen");
		PlayerSettingAPI.registerSetting(actionBarItemCooldown, menu);

		sideBarPearlCooldown = new BooleanSetting(plugin, false, "Show pearl cooldown in side bar",
				"finaleSideBarPearlCooldown",
				"Should pearl cooldown be shown on the sidebar");
		PlayerSettingAPI.registerSetting(sideBarPearlCooldown, menu);

		sideBarItemCooldown = new BooleanSetting(plugin, false, "Show item cooldown in side bar",
			"finaleSideBarItemCooldown",
			"Should items cooldown be shown on the sidebar");
		PlayerSettingAPI.registerSetting(sideBarItemCooldown, menu);

		showArmor = new BooleanSetting(Finale.getPlugin(), true, "Show armor durability", "finaleArmorDurabilitySidebar","Should the durability of your worn armor be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showArmor, menu);

		showTool = new BooleanSetting(Finale.getPlugin(), true, "Show tool durability", "finaleToolDurabilitySidebar","Should the durability of your held tool be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showTool, menu);

		showPotionEffects = new BooleanSetting(Finale.getPlugin(), true, "Show active potion effects", "finaleShowPotionsSidebar","Should active potion effects be shown in the scorebard");
		PlayerSettingAPI.registerSetting(showPotionEffects, menu);

		toolProtection = new BooleanSetting(Finale.getPlugin(), true, "Protect from breaking enchanted tools", "finaleToolProtection","Do you want to be given mining fatigue if mining with a low durability tool");
		PlayerSettingAPI.registerSetting(toolProtection, menu);

		coordsLocation = new DisplayLocationSetting(Finale.getPlugin(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "Coords location on HUD", "finaleCoordsLocation", new ItemStack(Material.ARROW), "Where to display your location");
		PlayerSettingAPI.registerSetting(coordsLocation, menu);

		toolProtectionThreshhold = new BoundedIntegerSetting(Finale.getPlugin(), 10, "Threshhold for tool protection", "finaleToolProtectionThreshhold",new ItemStack(Material.DIAMOND_PICKAXE), "Durability at which break protection should trigger", false, 1, 2000);
		PlayerSettingAPI.registerSetting(toolProtectionThreshhold, menu);
	}

	public boolean setVanillaPearlCooldown(UUID uuid) {
		return vanillaPearlCooldown.getValue(uuid);
	}

	public boolean setVanillaItemCooldown(UUID uuid) {
		return vanillaItemCooldown.getValue(uuid);
	}

	public int getToolProtectionThreshhold(UUID uuid) {
		return toolProtectionThreshhold.getValue(uuid);
	}

	public boolean useToolProtection(UUID uuid) {
		return toolProtection.getValue(uuid);
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

	public DisplayLocationSetting getCoordsLocation(){
		return coordsLocation;
	}

	public boolean sideBarPearlCooldown(UUID uuid) {
		return sideBarPearlCooldown.getValue(uuid);
	}

	public boolean sideBarItemCooldown(UUID uuid) {
		return sideBarItemCooldown.getValue(uuid);
	}

	public boolean actionBarPearlCooldown(UUID uuid) {
		return actionBarPearlCooldown.getValue(uuid);
	}

	public boolean actionBarItemCooldown(UUID uuid) {
		return actionBarItemCooldown.getValue(uuid);
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
