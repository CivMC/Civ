package com.untamedears.jukealert.gui;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.DormantCullingAppender;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.FastMultiPageView;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.MultiPageView;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.utilities.TextUtil;
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

	private List<IClickable> constructSnitchClickables(int start, int offset) {
		final List<IClickable> clickables = new LinkedList<>();
		for (int i = start; i <= Math.min(snitches.size() - 1, start + offset); i++) {
			Snitch snitch = snitches.get(i);
			// Base the snitch icon on the snitch type
			final var icon = snitch.getType().getItem().clone();
			ItemUtils.handleItemMeta(icon, (ItemMeta meta) -> {
				meta.displayName(Component.text(snitch.getName(), NamedTextColor.GOLD));
				final var location = snitch.getLocation();
				List<Component> lore = new ArrayList<>();
				lore.add(ChatUtils.newComponent("Location: ").color(NamedTextColor.AQUA)
						.append(Component.text(location.getWorld().getName() + " ").color(NamedTextColor.WHITE))
						.append(Component.text(location.getBlockX()).color(NamedTextColor.RED))
						.append(Component.text(", ").color(NamedTextColor.WHITE))
						.append(Component.text(location.getBlockY()).color(NamedTextColor.GREEN))
						.append(Component.text(", ").color(NamedTextColor.WHITE))
						.append(Component.text(location.getBlockZ()).color(NamedTextColor.BLUE)));
				lore.add(ChatUtils.newComponent("Group: " + snitch.getGroup().getName()).color(NamedTextColor.YELLOW));
				if (snitch.hasAppender(DormantCullingAppender.class)) {
					final var cull = snitch.getAppender(DormantCullingAppender.class);
					if (cull.isActive()) {
						lore.add(ChatUtils.newComponent("Will go dormant in " + TextUtil.formatDuration(cull.getTimeUntilDormant())).color(NamedTextColor.AQUA));
						MetaUtils.addGlow(meta);
					}
					else if (cull.isDormant()) {
						lore.add(ChatUtils.newComponent("Will cull in " + TextUtil.formatDuration(cull.getTimeUntilCulling())).color(NamedTextColor.AQUA));
					}
				}
				if (this.canShowDetails) {
					lore.add(ChatUtils.newComponent("Click to show details").color(NamedTextColor.GREEN));
				}
				lore.add(ChatUtils.newComponent("Right click to send waypoint").color(NamedTextColor.GOLD));
				meta.lore(lore);
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
		final var view = new FastMultiPageView(this.player, this::constructSnitchClickables, this.title, 6);
		view.setMenuSlot(SnitchLogGUI.constructExitClick(), 3);
		view.showScreen();
	}

}
