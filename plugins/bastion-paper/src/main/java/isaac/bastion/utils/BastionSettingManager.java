package isaac.bastion.utils;

import isaac.bastion.Bastion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.BooleanSetting;
import vg.civcraft.mc.civmodcore.playersettings.impl.DisplayLocationSetting;

import java.util.UUID;

public class BastionSettingManager {

	private BooleanSetting bsiOverlay;
	private BooleanSetting showNoBastion;
	private DisplayLocationSetting bsiLocation;

	public BastionSettingManager() {
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection("Bastion", "All settings related to Bastion");

		bsiOverlay = new BooleanSetting(Bastion.getPlugin(), false, "Display Bastion Information", "bsiOverlay", "Shows if the block your standing on is bastioned territory.");
		showNoBastion = new BooleanSetting(Bastion.getPlugin(), false, "Display if you are not in a bastion field", "showNoBastion", "If enabled, will display if you are not currently in a bastion field.");

		bsiLocation = new DisplayLocationSetting(Bastion.getPlugin(), DisplayLocationSetting.DisplayLocation.SIDEBAR, "BSI Location", "bsiLocation", new ItemStack(Material.ARROW), "Where to display BSI");

		PlayerSettingAPI.registerSetting(bsiOverlay, menu);
		PlayerSettingAPI.registerSetting(bsiLocation, menu);
		PlayerSettingAPI.registerSetting(showNoBastion, menu);
	}

	public BooleanSetting getBsiOverlay() {
		return bsiOverlay;
	}

	public DisplayLocationSetting getBsiLocation() {
		return bsiLocation;
	}

	public boolean getShowNoBastion(UUID uuid) {
		return showNoBastion.getValue(uuid);
	}
}
