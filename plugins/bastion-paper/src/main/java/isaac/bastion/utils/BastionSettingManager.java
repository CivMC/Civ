package isaac.bastion.utils;

import isaac.bastion.Bastion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;

public class BastionSettingManager {

	private BooleanSetting bsiOverlay;
	private DisplayLocationSetting bsiLocation;

	public BastionSettingManager() {
		initSettings();
	}

	public void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Bastion", "All settings related to Bastion");

		bsiOverlay = new BooleanSetting(Bastion.getPlugin(), false, "Display Bastion Information", "bsiOverlay", "Shows if the block your standing on is bastioned territory.");

		bsiLocation = new DisplayLocationSetting(Bastion.getPlugin(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "BSI Location", "bsiLocation", new ItemStack(Material.ARROW), "Where to display BSI");

		PlayerSettingAPI.registerSetting(bsiOverlay, menu);
		PlayerSettingAPI.registerSetting(bsiLocation, menu);
	}

	public BooleanSetting getBsiOverlay() {
		return bsiOverlay;
	}

	public DisplayLocationSetting getBsiLocation() {
		return bsiLocation;
	}
}
