package com.untamedears.JukeAlert.gui;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

import com.untamedears.JukeAlert.model.Snitch;

public class SnitchOverviewGUI {
	
	private List <Snitch> snitches;
	private Player player;
	
	public SnitchOverviewGUI(Player p, List <Snitch> snitches) {
		this.snitches = snitches;
		this.player = p;
	}
	
	public void showScreen() {
		MultiPageView view = new MultiPageView(player, constructSnitchClickables(), "Nearby snitches", true);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}
	
	private List <IClickable> constructSnitchClickables() {
		List <IClickable> clicks = new LinkedList<IClickable>();
		for(final Snitch snitch : snitches) {
			ItemStack is = new ItemStack(Material.JUKEBOX);
			ISUtils.setName(is, ChatColor.GOLD + snitch.getName());
			ISUtils.addLore(is, ChatColor.AQUA + "Located at " + snitch.getX() + ", " + snitch.getY() + ", " + snitch.getZ());
			ISUtils.addLore(is, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
			ISUtils.addLore(is, ChatColor.GREEN + "Click to view the logs");
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

}
