package com.untamedears.jukealert.gui;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;
import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.civmodcore.world.WorldUtils;

public class SnitchOverviewGUI {

	private final List<Snitch> snitches;
	private final Player player;
	private final String title;
	private final boolean canShowDetails;

	public SnitchOverviewGUI(Player player, List<Snitch> snitches, String title, boolean canShowDetails) {
		this.snitches = snitches;
		this.player = player;
		this.title = title;
		this.canShowDetails = canShowDetails;
	}

	private List<IClickable> constructSnitchClickables() {
		final List<IClickable> clickables = new LinkedList<>();
		for (final Snitch snitch : this.snitches) {
			// Base the snitch icon on the snitch type
			final var icon = snitch.getType().getItem().clone();
			ItemUtils.handleItemMeta(icon, (ItemMeta meta) -> {
				meta.setDisplayName(ChatColor.GOLD + snitch.getName());
				final var location = snitch.getLocation();
				MetaUtils.addLore(meta, ChatColor.AQUA + "Location: "
						+ ChatColor.WHITE + location.getWorld().getName() + " "
						+ ChatColor.RED + location.getBlockX()
						+ ChatColor.WHITE + ", "
						+ ChatColor.GREEN + location.getBlockY()
						+ ChatColor.WHITE + ", "
						+ ChatColor.BLUE + location.getBlockZ());
				MetaUtils.addLore(meta, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
				if (snitch.hasAppender(DormantCullingAppender.class)) {
					final var cull = snitch.getAppender(DormantCullingAppender.class);
					if (cull.isActive()) {
						MetaUtils.addLore(meta, ChatColor.AQUA + "Will go dormant in " +
								TextUtil.formatDuration(cull.getTimeUntilDormant()));
						MetaUtils.addGlow(meta);
					}
					else if (cull.isDormant()) {
						MetaUtils.addLore(meta, ChatColor.AQUA + "Will cull in " +
								TextUtil.formatDuration(cull.getTimeUntilCulling()));
					}
				}
				if (this.canShowDetails) {
					MetaUtils.addLore(meta, ChatColor.GREEN + "Click to show details");
				}
				MetaUtils.addLore(meta, ChatColor.GOLD + "Right click to send waypoint");
				return true;
			});
			clickables.add(new Clickable(icon) {
				@Override
				public void clicked(final Player clicker) {
					if (canShowDetails) {
						new SnitchLogGUI(clicker, snitch).showScreen();
					}
				}
				@Override
				protected void onRightClick(final Player clicker) {
					final var location = snitch.getLocation();
					if (!WorldUtils.doLocationsHaveSameWorld(location, clicker.getLocation())) {
						clicker.sendMessage(ChatColor.RED + "That snitch is in a different world!");
						return;
					}
					clicker.sendMessage("["
							+ "name:" + snitch.getName() + ","
							+ "x:" + location.getBlockX() + ","
							+ "y:" + location.getBlockY() + ","
							+ "z:" + location.getBlockZ()
							+ "]");
				}
			});
		}
		return clickables;
	}

	public void showScreen() {
		final var view = new MultiPageView(this.player, constructSnitchClickables(), this.title, true);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}

}
