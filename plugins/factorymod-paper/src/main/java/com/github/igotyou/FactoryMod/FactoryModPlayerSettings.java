package com.github.igotyou.FactoryMod;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.playersettings.impl.EnumSetting;

import java.util.UUID;

public class FactoryModPlayerSettings {

	private final FactoryMod plugin;
	EnumSetting<IoConfigDirectionMode> ioDirectionSetting;

	public FactoryModPlayerSettings(FactoryMod plugin) {
		this.plugin = plugin;
		initSettings();
	}

	private void initSettings() {
		MenuSection menu = PlayerSettingAPI.getMainMenu().createMenuSection(
				"FactoryMod",
				"FactoryMod settings",
				new ItemStack(Material.FURNACE)
		);

		ioDirectionSetting = new EnumSetting<>(
				plugin,
				IoConfigDirectionMode.VISUAL_RELATIVE,
				"Factory IOConfig Mode",
				"ioconfig_visual_mode",
				new ItemStack(Material.HOPPER),
				"Change how the factory IO config appears",
				true,
				IoConfigDirectionMode.class
		);
		PlayerSettingAPI.registerSetting(ioDirectionSetting, menu);
	}

	public IoConfigDirectionMode getIoDirectionMode(UUID id) {
		return ioDirectionSetting.getValue(id);
	}

	public enum IoConfigDirectionMode {

		VISUAL_RELATIVE(
				"Relative Directions",
				new String[] {
						"The furnace shows the",
						"front of the factory."
				}),
		CARDINAL(
				"Cardinal Directions",
				new String[] {
						"Cardinals (for those too",
						"familiar with map mods.)"
				});

		public final String simpleDescription;
		public final String[] fullDescription;

		private IoConfigDirectionMode(String simpleDescription, String[] fullDescription) {
			this.simpleDescription = simpleDescription;
			this.fullDescription = fullDescription;
		}
	}

}
