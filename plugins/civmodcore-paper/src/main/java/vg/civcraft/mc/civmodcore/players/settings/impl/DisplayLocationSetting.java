package vg.civcraft.mc.civmodcore.players.settings.impl;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;

public class DisplayLocationSetting extends LimitedStringSetting {

	private String displayName;

	public DisplayLocationSetting(JavaPlugin plugin, DisplayLocation defaultValue, String name, String identifier,
			ItemStack gui, String displayName) {
		super(plugin, defaultValue.toString(), name, identifier, gui, "Set where to display " + displayName,
				Arrays.stream(DisplayLocation.values()).map(DisplayLocation::toString).collect(Collectors.toList()),
				false);
		this.displayName = displayName;
	}
	
	public boolean showOnSidebar(UUID uuid) {
		DisplayLocation location = DisplayLocation.valueOf(getValue(uuid));
		return location == DisplayLocation.SIDEBAR || location == DisplayLocation.BOTH;
	}
	
	public boolean showOnActionbar(UUID uuid) {
		DisplayLocation location = DisplayLocation.valueOf(getValue(uuid));
		return location == DisplayLocation.ACTIONBAR || location == DisplayLocation.BOTH;
	}
	
	public DisplayLocation getDisplayLocation(UUID uuid) {
		return DisplayLocation.valueOf(getValue(uuid));
	}

	@Override
	public void handleMenuClick(Player player, MenuSection menu) {
		DisplayLocation currentValue = DisplayLocation.fromString(getValue(player));
		IClickable sideClick = genLocationClick(Material.YELLOW_BANNER, "%sShow %s only on side bar",
				DisplayLocation.SIDEBAR, menu, currentValue);
		IClickable actionClick = genLocationClick(Material.STONE_PRESSURE_PLATE, "%sShow %s only on action bar",
				DisplayLocation.ACTIONBAR, menu, currentValue);
		IClickable bothClick = genLocationClick(Material.PAINTING, "%sShow %s both on action and side bar",
				DisplayLocation.BOTH, menu, currentValue);
		IClickable noneClick = genLocationClick(Material.BARRIER, "%sShow %s neither on side bar, nor action bar",
				DisplayLocation.NONE, menu, currentValue);
		MultiPageView selector = new MultiPageView(player, Arrays.asList(sideClick, actionClick, bothClick, noneClick),
				"Select where to show " + displayName, true);
		selector.showScreen();
	}

	private IClickable genLocationClick(Material mat, String infoText, DisplayLocation location, MenuSection menu, DisplayLocation currentlySelect) {
		ItemStack sideStack = new ItemStack(mat);
		ItemUtils.setDisplayName(sideStack, String.format(infoText, ChatColor.GOLD, displayName));
		if (location == currentlySelect) {
			ItemUtils.addGlow(sideStack);
		}
		return new LClickable(sideStack, p -> {
			setValue(p, location.toString());
			menu.showScreen(p);
		});
	}

	public enum DisplayLocation {
		SIDEBAR, ACTIONBAR, BOTH, NONE;

		public static DisplayLocation fromString(String s) {
			switch (s.toUpperCase()) {
			case "SIDEBAR":
				return SIDEBAR;
			case "ACTIONBAR":
				return ACTIONBAR;
			case "BOTH":
				return BOTH;
			case "NONE":
			case "NEITHER":
				return NONE;
			default:
				return null;
			}
		}
	}

}
