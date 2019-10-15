package com.untamedears.jukealert.gui;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.util.TextUtil;

public class SnitchOverviewGUI {

	private List<Snitch> snitches;

	private Player player;
	private String title;
	private boolean canShowDetails;

	public SnitchOverviewGUI(Player p, List<Snitch> snitches, String title, boolean canShowDetails) {
		this.snitches = snitches;
		this.player = p;
		this.title = title;
		this.canShowDetails = canShowDetails;
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
			if (snitch.hasAppender(DormantCullingAppender.class)) {
				DormantCullingAppender cull = (DormantCullingAppender) snitch.getAppender(DormantCullingAppender.class);
				String timeInfo;
				if (cull.isActive()) {
					timeInfo = "Will go dormant in "
							+ TextUtil.formatDuration(cull.getTimeUntilDormant());
				} else if (cull.isDormant()) {
					timeInfo = "Will cull in " + TextUtil.formatDuration(cull.getTimeUntilCulling());
				} else {
					// dead
					continue;
				}
				ItemAPI.addLore(is, ChatColor.AQUA + timeInfo);
			}
			if (canShowDetails) {
				ItemAPI.addLore(is, ChatColor.GREEN + "Click to show details");
				clicks.add(new Clickable(is) {
					@Override
					public void clicked(Player p) {
						SnitchLogGUI gui = new SnitchLogGUI(player, snitch);
						gui.showScreen();
					}
				});
			} else {
				clicks.add(new DecorationStack(is));
			}
		}
		return clicks;
	}

	public void showScreen() {
		MultiPageView view = new MultiPageView(player, constructSnitchClickables(), title, true);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}
}
