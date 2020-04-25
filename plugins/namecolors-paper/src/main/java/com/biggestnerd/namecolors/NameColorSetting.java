package com.biggestnerd.namecolors;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public class NameColorSetting extends PlayerSetting<ChatColor> {

	private static Map<ChatColor, Material> colorToGui = new EnumMap<>(ChatColor.class);
	private static final String RAINBOW_PERMISSION = "namecolor.rainbow";
	private static final String COLOR_PERMISSION = "namecolor.use";

	public static final ChatColor RAINBOW_COLOR = ChatColor.STRIKETHROUGH;

	static {
		colorToGui.put(ChatColor.DARK_RED, Material.RED_WOOL);
		colorToGui.put(ChatColor.DARK_GREEN, Material.GREEN_WOOL);
		colorToGui.put(ChatColor.BLUE, Material.BLUE_WOOL);
		colorToGui.put(ChatColor.DARK_PURPLE, Material.PURPLE_WOOL);
		colorToGui.put(ChatColor.DARK_AQUA, Material.CYAN_WOOL);
		colorToGui.put(ChatColor.GRAY, Material.LIGHT_GRAY_WOOL);
		colorToGui.put(ChatColor.DARK_GRAY, Material.GRAY_WOOL);
		colorToGui.put(ChatColor.GREEN, Material.LIME_WOOL);
		colorToGui.put(ChatColor.YELLOW, Material.YELLOW_WOOL);
		colorToGui.put(ChatColor.AQUA, Material.LIGHT_BLUE_WOOL);
		colorToGui.put(ChatColor.GOLD, Material.YELLOW_GLAZED_TERRACOTTA);
		colorToGui.put(ChatColor.LIGHT_PURPLE, Material.MAGENTA_WOOL);
		colorToGui.put(ChatColor.RESET, Material.WHITE_WOOL);
	}

	public NameColorSetting(JavaPlugin owningPlugin) {
		super(owningPlugin, ChatColor.RESET, "Name color", "namecolors.choser", new ItemStack(Material.RED_WOOL),
				"Lets you chose the color of your name", true);
	}

	@Override
	public ChatColor deserialize(String serial) {
		return ChatColor.valueOf(serial);
	}

	@Override
	public ItemStack getGuiRepresentation(UUID player) {
		ChatColor color = getValue(player);
		ItemStack item;
		if (color == RAINBOW_COLOR) {
			item = new ItemStack(Material.YELLOW_STAINED_GLASS);
		} else {
			if (color != null) {
				item = new ItemStack(colorToGui.get(color));
			} else {
				item = new ItemStack(Material.WHITE_WOOL);
			}
		}
		applyInfoToItemStack(item, player);
		Player play = Bukkit.getPlayer(player);
		if (play != null && !play.hasPermission(COLOR_PERMISSION) && !play.hasPermission(RAINBOW_PERMISSION)) {
			ItemAPI.addLore(item, ChatColor.RED + "You do not have permission to do this");
		}
		return item;
	}

	@Override
	public void handleMenuClick(Player player, MenuSection menu) {
		List<IClickable> clicks = new ArrayList<>();
		if (player.hasPermission(COLOR_PERMISSION)) {

			for (Entry<ChatColor, Material> entry : colorToGui.entrySet()) {
				ItemStack is = new ItemStack(entry.getValue());
				ItemAPI.setDisplayName(is,
						"Change to color of your name to " + entry.getKey() + entry.getKey().name());
				clicks.add(new Clickable(is) {

					@Override
					public void clicked(Player p) {
						player.sendMessage(
								"The color of your name was changed to " + entry.getValue() + entry.getValue().name());
						setValue(player, entry.getKey());
					}
				});
			}
		}
		if (player.hasPermission(RAINBOW_PERMISSION)) {
			ItemStack is = new ItemStack(Material.YELLOW_STAINED_GLASS);
			ItemAPI.setDisplayName(is, "Change to color of your name to " + NameColors.rainbowify("rainbow"));
			clicks.add(new Clickable(is) {

				@Override
				public void clicked(Player p) {
					player.sendMessage("The color of your name was changed to " + NameColors.rainbowify("rainbow"));
					setValue(player, RAINBOW_COLOR);
				}
			});
		}
		if (clicks.isEmpty()) {
			player.sendMessage(ChatColor.RED + "You do not have permission to change the color of your name");
			return;
		}
		MultiPageView view = new MultiPageView(player, clicks, "Select a name color", true);
		ItemStack returnStack = new ItemStack(Material.BOOK);
		ItemAPI.setDisplayName(returnStack, "Return to previous menu");
		view.setMenuSlot(new Clickable(returnStack) {

			@Override
			public void clicked(Player p) {
				menu.showScreen(player);
			}
		}, 0);
		view.showScreen();
	}

	@Override
	public boolean isValidValue(String input) {
		try {
			ChatColor color = ChatColor.valueOf(input);
			return color == RAINBOW_COLOR || colorToGui.containsKey(color);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	@Override
	public String serialize(ChatColor value) {
		return value.name();
	}

	@Override
	public void setValue(UUID player, ChatColor value) {
		super.setValue(player, value);
		Player play = Bukkit.getPlayer(player);
		if (play != null) {
			NameColors.getInstance().updatePlayerName(play, value);
		}
	}

	@Override
	public String toText(ChatColor value) {
		return value.name();
	}

}
