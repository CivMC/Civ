package com.untamedears.JukeAlert.gui;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.model.Snitch;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;

public class SnitchOverviewGUI {

	private List<Snitch> snitches;

	private Player player;

	public SnitchOverviewGUI(Player p, List<Snitch> snitches) {
		this.snitches = snitches;
		this.player = p;
	}

	private List<IClickable> constructSnitchClickables() {
		List<IClickable> clicks = new LinkedList<>();
		for (final Snitch snitch : snitches) {
			ItemStack is = new ItemStack(Material.JUKEBOX);
			ItemAPI.setDisplayName(is, ChatColor.GOLD + snitch.getName());
			Location loc = snitch.getLocation();
			ItemAPI.addLore(is,
					ChatColor.AQUA + "Located at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
			ItemAPI.addLore(is, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
			ItemAPI.addLore(is, ChatColor.GREEN + "Click to view the logs");
			clicks.add(new Clickable(is) {
				@Override
				public void clicked(Player p) {
					SnitchLogGUI gui = new SnitchLogGUI(player, snitch);
					gui.showScreen();
				}
			});
		}
		return clicks;
	}

	public void showScreen() {
		MultiPageView view = new MultiPageView(player, constructSnitchClickables(), "Nearby snitches", true);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}
}
