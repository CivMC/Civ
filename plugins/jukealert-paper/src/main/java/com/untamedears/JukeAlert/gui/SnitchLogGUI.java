package com.untamedears.JukeAlert.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

import com.untamedears.JukeAlert.JukeAlert;
import com.untamedears.JukeAlert.command.commands.ClearCommand;
import com.untamedears.JukeAlert.model.LoggedAction;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.model.SnitchAction;

public class SnitchLogGUI {

	private Player player;

	private Snitch snitch;

	private List<SnitchAction> actions;

	public SnitchLogGUI(Player p, Snitch snitch) {

		this.player = p;
		this.snitch = snitch;
		this.actions = JukeAlert.getInstance().getJaLogger().getAllSnitchLogs(snitch);
	}

	public void showScreen() {

		MultiPageView view = new MultiPageView(player, constructContent(), snitch.getName().substring(0,
			Math.min(32, snitch.getName().length())), true);
		view.setMenuSlot(constructClearClick(), 1);
		if (snitch.shouldLog()) {
			view.setMenuSlot(constructLeverToggleClick(), 5);
		}
		view.setMenuSlot(constructNameChanceClick(), 2);
		view.setMenuSlot(constructExitClick(), 4);
		view.setMenuSlot(constructInfoStack(), 3);
		view.showScreen();
	}

	private IClickable constructInfoStack() {

		ItemStack is = new ItemStack(Material.PAPER);
		ISUtils.setName(is, ChatColor.GOLD + "Logs for " + snitch.getName());
		ISUtils.addLore(
			is, ChatColor.AQUA + "Located at " + snitch.getX() + ", " + snitch.getY() + ", " + snitch.getZ());
		ISUtils.addLore(is, ChatColor.YELLOW + "Group: " + snitch.getGroup().getName());
		return new DecorationStack(is);
	}

	public static IClickable constructExitClick() {

		ItemStack is = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(is, ChatColor.GOLD + "Exit");
		ISUtils.addLore(is, ChatColor.AQUA + "Click to exit GUI");
		return new Clickable(is) {
			@Override
			public void clicked(Player p) {
				ClickableInventory.forceCloseInventory(p);
			}
		};
	}

	private IClickable constructClearClick() {

		ItemStack is = new ItemStack(Material.TNT);
		ISUtils.setName(is, ChatColor.GOLD + "Clear all logs");
		IClickable click = new Clickable(is) {
			@Override
			public void clicked(Player p) {
				Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), new Runnable() {
					@Override
					public void run() {
						ClearCommand.deleteLog(player, snitch);
					}
				});
				ClickableInventory.forceCloseInventory(player);
			}
		};
		return click;
	}

	private IClickable constructNameChanceClick() {

		ItemStack is = new ItemStack(Material.SIGN);
		ISUtils.setName(is, ChatColor.GOLD + "Rename this snitch");
		IClickable click = new Clickable(is) {
			@Override
			public void clicked(Player p) {
				p.sendMessage(ChatColor.YELLOW + "Please enter a new name for the snitch:");
				ClickableInventory.forceCloseInventory(p);
				new Dialog(p, JukeAlert.getInstance()) {
					@Override
					public List<String> onTabComplete(String wordCompleted, String[] fullMessage) {
						return null;
					}

					@Override
					public void onReply(String[] message) {
						if (message.length != 1) {
							player.sendMessage(ChatColor.RED + "Snitch names may only be a single word");
							showScreen();
							return;
						}
						final String newName = message[0];
						snitch.setName(newName);
						Bukkit.getScheduler().runTaskAsynchronously(JukeAlert.getInstance(), new Runnable() {
							@Override
							public void run() {
								JukeAlert.getInstance().getJaLogger().updateSnitchName(snitch, newName);
							}
						});
						player.sendMessage(ChatColor.AQUA + " Changed snitch name to " + newName);
						showScreen();
					}
				};
			}
		};
		return click;
	}

	private IClickable constructLeverToggleClick() {

		ItemStack is = new ItemStack(Material.LEVER);
		ISUtils.setName(is, ChatColor.GOLD + "Toggle lever activation by redstone");
		ISUtils.addLore(is, ChatColor.AQUA + "Currently turned " + (snitch.shouldToggleLevers() ? "on" : "off"));
		IClickable click = new Clickable(is) {
			@Override
			public void clicked(Player p) {
				boolean currentState = snitch.shouldToggleLevers();
				JukeAlert.getInstance().getJaLogger().updateSnitchToggleLevers(snitch, !currentState);
				snitch.setShouldToggleLevers(!currentState);
				p.sendMessage(ChatColor.GREEN + "Toggled lever activation " + (currentState ? "off" : "on"));
				showScreen();
			}
		};
		return click;
	}

	private List<IClickable> constructContent() {

		List<IClickable> clicks = new ArrayList<IClickable>();
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		for (SnitchAction action : actions) {
			ItemStack is;
			switch (action.getAction()) {
				case BLOCK_BREAK:
					is = new ItemStack(action.getMaterial());
					ISUtils.setName(is, ChatColor.GOLD + action.getMaterial().toString()
					                                   + " broken by " + action.getInitiateUser());
					break;
				case BLOCK_BURN:
					is = new ItemStack(action.getMaterial());
					ISUtils.setName(is, ChatColor.GOLD + action.getMaterial().toString() + " was destroyed by fire");
					break;
				case BLOCK_PLACE:
					is = new ItemStack(action.getMaterial());
					ISUtils.setName(is, ChatColor.GOLD + action.getMaterial().toString()
					                                   + " placed by " + action.getInitiateUser());
					break;
				case BLOCK_USED:
					is = new ItemStack(action.getMaterial());
					ISUtils.setName(is,
							ChatColor.GOLD + action.getMaterial().toString() + " used by " + action.getInitiateUser());
					break;
				case BUCKET_EMPTY:
				case BUCKET_FILL:
					String bucketName;
					if (action.getMaterial() == null) {
						is = new ItemStack(Material.WATER_BUCKET);
						bucketName = "Bucket";
					} else {
						is = new ItemStack(action.getMaterial());
						bucketName = action.getMaterial().toString();
					}
					if (is.getItemMeta() == null) {
						// This is needed due to uncreatable itemstacks being stored as used item
						is = new ItemStack(Material.WATER_BUCKET);
					}
					String bucketAction = action.getAction() == LoggedAction.BUCKET_EMPTY ? "emptied" : "filled";
					ISUtils.setName(is,
							ChatColor.GOLD + bucketName + " " + bucketAction + " by " + action.getInitiateUser());
					break;
				case ENTITY_DISMOUNT:
				case ENTITY_MOUNT:
					is = new ItemStack(Material.SADDLE);
					String horseAction = action.getAction() == LoggedAction.ENTITY_MOUNT ? "mounted" : "dismounted";
					ISUtils.setName(is, ChatColor.GOLD + action.getVictim()
					                                   + " " + horseAction + " by " + action.getInitiateUser());
					break;
				case ENTRY:
					is = new ItemStack(Material.COMPASS);
					ISUtils.setName(is, ChatColor.GOLD + action.getInitiateUser() + " entered");
					break;
				case EXCHANGE:
					is = new ItemStack(Material.CHEST);
					ISUtils.setName(is, ChatColor.GOLD + action.getInitiateUser() + " used an ItemExchange");
					break;
				case IGNITED:
					is = new ItemStack(Material.FLINT_AND_STEEL);
					ISUtils.setName(is, ChatColor.GOLD + action.getMaterial().toString()
					                                   + " ignited by " + action.getInitiateUser());
					break;
				case KILL:
					is = new ItemStack(Material.DIAMOND_SWORD);
					ItemMeta im = is.getItemMeta();
					im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					is.setItemMeta(im);
					ISUtils.setName(is,
							ChatColor.GOLD + action.getVictim().toString() + " killed by " + action.getInitiateUser());
					break;
				case LOGIN:
					is = new ItemStack(Material.EYE_OF_ENDER);
					ISUtils.setName(is, ChatColor.GOLD + action.getInitiateUser() + " logged in");
					break;
				case LOGOUT:
					is = new ItemStack(Material.EYE_OF_ENDER);
					ISUtils.setName(is, ChatColor.GOLD + action.getInitiateUser() + " logged out");
					break;
				case UNKNOWN:
				case USED:
					// Currently unused
					continue;
				case VEHICLE_DESTROY:
					is = new ItemStack(Material.MINECART);
					ISUtils.setName(is, ChatColor.GOLD + action.getInitiateUser() + " broke " + action.getVictim());
					break;
				default:
					continue;
			}
			ISUtils.addLore(is, ChatColor.AQUA + "At " + action.getX() + ", " + action.getY() + ", " + action.getZ());
			ISUtils.addLore(is, ChatColor.YELLOW + "Time: " + dateFormat.format(action.getDate()));
			clicks.add(new DecorationStack(is));
		}
		return clicks;
	}
}
