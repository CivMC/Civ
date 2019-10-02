package com.untamedears.JukeAlert.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.actions.LoggableAction;
import com.untamedears.JukeAlert.model.actions.SnitchAction;
import com.untamedears.JukeAlert.model.appender.LeverToggleAppender;
import com.untamedears.JukeAlert.model.appender.SnitchLogAppender;
import com.untamedears.JukeAlert.util.JukeAlertPermissionHandler;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;

public class SnitchLogGUI {

	public static IClickable constructExitClick() {
		ItemStack is = new ItemStack(Material.OAK_DOOR);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Exit");
		ItemAPI.addLore(is, ChatColor.AQUA + "Click to exit GUI");
		return new Clickable(is) {
			@Override
			public void clicked(Player p) {
				ClickableInventory.forceCloseInventory(p);
			}
		};
	}

	private Player player;
	private Snitch snitch;
	private List<LoggableAction> actions;
	private SnitchLogAppender logAppender;

	public SnitchLogGUI(Player p, Snitch snitch) {
		this.player = p;
		this.snitch = snitch;
		this.logAppender = (SnitchLogAppender) snitch.getAppender(SnitchLogAppender.class);
		if (logAppender != null) {
			this.actions = logAppender.getFullLogs();
		}
	}

	private IClickable constructClearClick() {
		ItemStack is = new ItemStack(Material.TNT);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Clear all logs");
		if (snitch.hasPermission(player, JukeAlertPermissionHandler.getClearLogs())) {
			return new Clickable(is) {
				@Override
				public void clicked(Player p) {
					if (snitch.hasPermission(player, JukeAlertPermissionHandler.getClearLogs())) {
						logAppender.deleteLogs();
						ClickableInventory.forceCloseInventory(player);
					}
				}
			};
		}
		ItemAPI.addLore(is, ChatColor.RED + "You do not have permission to do this");
		return new DecorationStack(is);

	}

	private List<IClickable> constructContent() {
		List<IClickable> clicks = new ArrayList<>();
		for (LoggableAction action : actions) {
			clicks.add(action.getGUIRepresentation());
		}
		return clicks;
	}

	private IClickable constructInfoStack() {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Logs for " + snitch.getName());
		Location loc = snitch.getLocation();
		ItemAPI.addLore(is,
				ChatColor.AQUA + "Located at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
		ItemAPI.addLore(is, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
		ItemAPI.addLore(is, ChatColor.YELLOW + "Type: " + snitch.getType().getName());
		return new DecorationStack(is);
	}

	private IClickable constructLeverToggleClick() {
		LeverToggleAppender leverAppender = (LeverToggleAppender) snitch.getAppender(LeverToggleAppender.class);
		ItemStack is = new ItemStack(Material.LEVER);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Toggle lever activation by redstone");
		ItemAPI.addLore(is, ChatColor.AQUA + "Currently turned " + ((leverAppender.shouldToggle()) ? "on" : "off"));
		if (snitch.hasPermission(player, JukeAlertPermissionHandler.getToggleLevers())) {
			return new Clickable(is) {
				@Override
				public void clicked(Player p) {
					if (snitch.hasPermission(player, JukeAlertPermissionHandler.getToggleLevers())) {
						leverAppender.switchState();
						p.sendMessage(ChatColor.GREEN + "Toggled lever activation "
								+ (leverAppender.shouldToggle() ? "off" : "on"));
						showScreen();
					}
				}
			};
		}
		ItemAPI.addLore(is, ChatColor.RED + "You do not have permission to do this");
		return new DecorationStack(is);
	}

	private IClickable constructNameChangeClick() {
		ItemStack is = new ItemStack(Material.OAK_SIGN);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Rename this snitch");
		return new Clickable(is) {
			@Override
			public void clicked(Player p) {
				p.sendMessage(ChatColor.YELLOW + "Please enter a new name for the snitch:");
				ClickableInventory.forceCloseInventory(p);
				new Dialog(p, JukeAlert.getInstance()) {
					@Override
					public void onReply(String[] message) {
						if (message.length != 1) {
							player.sendMessage(ChatColor.RED + "Snitch names may only be a single word");
							showScreen();
							return;
						}
						final String newName = message[0];
						snitch.setName(newName);
						player.sendMessage(ChatColor.AQUA + " Changed snitch name to " + newName);
						showScreen();
					}

					@Override
					public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
						return null;
					}
				};
			}
		};
	}

	public void showScreen() {
		MultiPageView view = new MultiPageView(player, constructContent(),
				snitch.getName().substring(0, Math.min(32, snitch.getName().length())), true);
		if (logAppender != null) {
			view.setMenuSlot(constructClearClick(), 1);
		}
		if (snitch.hasAppender(LeverToggleAppender.class)) {
			view.setMenuSlot(constructLeverToggleClick(), 5);
		}
		view.setMenuSlot(constructNameChangeClick(), 2);
		view.setMenuSlot(constructExitClick(), 4);
		view.setMenuSlot(constructInfoStack(), 3);
		view.showScreen();
	}
}
