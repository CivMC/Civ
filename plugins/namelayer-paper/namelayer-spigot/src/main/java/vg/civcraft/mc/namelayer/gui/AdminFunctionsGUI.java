package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import vg.civcraft.mc.civmodcore.chat.dialog.Dialog;
import vg.civcraft.mc.civmodcore.inventory.gui.Clickable;
import vg.civcraft.mc.civmodcore.inventory.gui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.commands.TransferGroup;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class AdminFunctionsGUI extends AbstractGroupGUI {

	private MainGroupGUI parent;

	public AdminFunctionsGUI(Player p, Group g, MainGroupGUI parent) {
		super(g, p);
		this.parent = parent;
	}

	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		// linking
		ItemStack linkStack = new ItemStack(Material.GOLD_INGOT);
		ItemUtils.setDisplayName(linkStack, ChatColor.GOLD + "Link group");
		Clickable linkClick;

		ItemUtils.addLore(linkStack, ChatColor.RED
									+ "Sorry, group linking is not a currently supported feature.");
							linkClick = new DecorationStack(linkStack);

		ci.setSlot(linkClick, 10);
		// merging
		ItemStack mergeStack = new ItemStack(Material.SPONGE);
		ItemUtils.setDisplayName(mergeStack, ChatColor.GOLD + "Merge group");
		Clickable mergeClick;
		
		ItemUtils.addLore(mergeStack, ChatColor.RED
									+ "Sorry, group merging is not a currently supported feature.");
							mergeClick = new DecorationStack(mergeStack);
		
//		if (gm.hasAccess(g, p.getUniqueId(),
//				PermissionType.getPermission("MERGE"))) {
//			mergeClick = new Clickable(mergeStack) {
//				@Override
//				public void clicked(Player p) {
//					showMergingMenu();
//				}
//			};
//		} else {
//			ItemUtils.addLore(mergeStack, ChatColor.RED
//					+ "You don't have permission to do this");
//			mergeClick = new DecorationStack(mergeStack);
//		}
		ci.setSlot(mergeClick, 12);
		// transferring group
		ItemStack transferStack = new ItemStack(Material.PACKED_ICE);
		ItemUtils.setDisplayName(transferStack, ChatColor.GOLD
				+ "Transfer group to new primary owner");
		Clickable transferClick;
		if (g.isOwner(p.getUniqueId())) {
			transferClick = new Clickable(transferStack) {
				@Override
				public void clicked(Player p) {
					showTransferingMenu();
				}
			};
		} else {
			ItemUtils.addLore(transferStack, ChatColor.RED
					+ "You don't have permission to do this");
			transferClick = new DecorationStack(transferStack);
		}
		ci.setSlot(transferClick, 14);
		// deleting group
		ItemStack deletionStack = new ItemStack(Material.BARRIER);
		ItemUtils.setDisplayName(deletionStack, ChatColor.GOLD + "Delete group");
		Clickable deletionClick;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("DELETE"))) {
			deletionClick = new Clickable(deletionStack) {
				@Override
				public void clicked(Player p) {
					showDeletionMenu();
				}
			};
		} else {
			ItemUtils.addLore(deletionStack, ChatColor.RED
					+ "You don't have permission to do this");
			deletionClick = new DecorationStack(deletionStack);
		}
		ci.setSlot(deletionClick, 16);

		// back button
		ItemStack backToOverview = goBackStack(); 
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Back to overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

//	private void showLinkingMenu() {
//		LinkingGUI lgui = new LinkingGUI(g, p, this);
//		lgui.showScreen();
//	}

	private void showMergingMenu() {
		MergeGUI mGui = new MergeGUI(g, p, this);
		mGui.showScreen();
	}

	private void showTransferingMenu() {
		p.sendMessage(ChatColor.GOLD
				+ "Enter the name of the new primary owner or \"cancel\" to exit this prompt");
		ClickableInventory.forceCloseInventory(p);
		new Dialog(p, NameLayerPlugin.getInstance()) {

			@Override
			public List<String> onTabComplete(String word, String[] arg1) {
				List<String> players = Bukkit.getOnlinePlayers().stream()
						.filter(p -> g.isMember(p.getUniqueId()))
						.map(Player::getName)
						.collect(Collectors.toList());
				players.add("cancel");

				return StringUtil.copyPartialMatches(word, players, new ArrayList<>());
			}

			@Override
			public void onReply(String[] arg0) {
				if (arg0.length > 1) {
					p.sendMessage(ChatColor.RED
							+ "You may only enter one player to transfer to");
					showScreen();
					return;
				}
				if (arg0[0].equals("cancel")) {
					showScreen();
					return;
				}
				final UUID transferUUID = NameAPI.getUUID(arg0[0]);
				if (transferUUID == null) {
					p.sendMessage(ChatColor.RED + "This player doesn't exist");
					showScreen();
					return;
				}
				final String playerName = NameAPI.getCurrentName(transferUUID);
				ClickableInventory confirmInv = new ClickableInventory(27,
						g.getName());
				ItemStack info = new ItemStack(Material.PAPER);
				ItemUtils.setDisplayName(info, ChatColor.GOLD + "Transfer group to "
						+ playerName);
				ItemUtils.addLore(info, ChatColor.RED
						+ "Are you sure that you want to", ChatColor.RED
						+ "transfer this group? You can not undo this!");
				ItemStack yes = yesStack();
				ItemUtils.setDisplayName(yes,
						ChatColor.GOLD + "Yes, transfer " + g.getName()
								+ " to " + playerName);
				ItemStack no = noStack();
				ItemUtils.setDisplayName(no,
						ChatColor.GOLD + "No, don't transfer " + g.getName());
				confirmInv.setSlot(new Clickable(yes) {

					@Override
					public void clicked(Player p) {
						if (TransferGroup.attemptTransfer(g, p, transferUUID)) {
							NameLayerPlugin.log(
									Level.INFO,
									p.getName()
											+ " transferred group to "
											+ NameAPI
													.getCurrentName(transferUUID)
											+ " for group " + g.getName()
											+ " via the gui");
						}
						else {
							showScreen();
						}
					}
				}, 11);
				confirmInv.setSlot(new Clickable(no) {

					@Override
					public void clicked(Player p) {
						showScreen();
					}
				}, 15);
				confirmInv.setSlot(new DecorationStack(info), 4);
				confirmInv.showInventory(p);

			}
		};
	}

	private void showDeletionMenu() {
		ClickableInventory confirmInv = new ClickableInventory(27, g.getName());
		ItemStack info = new ItemStack(Material.PAPER);
		ItemUtils.setDisplayName(info, ChatColor.GOLD + "Delete group");
		ItemUtils.addLore(info, ChatColor.RED + "Are you sure that you want to",
				ChatColor.RED + "delete this group? You can not undo this!");
		
		ItemStack yes = yesStack();
		ItemUtils.setDisplayName(yes, ChatColor.GOLD + "Yes, delete " + g.getName());
		ItemStack no = noStack();
		ItemUtils.setDisplayName(no, ChatColor.GOLD + "No, keep " + g.getName());
		confirmInv.setSlot(new Clickable(yes) {

			@Override
			public void clicked(Player p) {
				if (!gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("DELETE"))) {
					p.sendMessage(ChatColor.RED
							+ "You don't have permission to delete this group");
					showScreen();
					return;
				}
				if (g.isDisciplined()) {
					p.sendMessage(ChatColor.RED + "This group is disciplined.");
					showScreen();
					return;
				}
				NameLayerPlugin.log(Level.INFO,
						p.getName() + " deleted " + g.getName() + " via the gui");
				if (gm.deleteGroup(g.getName())) {
					p.sendMessage(ChatColor.GREEN + g.getName()
							+ " was successfully deleted.");
				} else {
					p.sendMessage(ChatColor.GREEN + "Group is now disciplined."
							+ " Check back later to see if group is deleted.");
				}
			}
		}, 11);
		confirmInv.setSlot(new Clickable(no) {

			@Override
			public void clicked(Player p) {
				showScreen();
			}
		}, 15);
		confirmInv.setSlot(new DecorationStack(info), 4);
		confirmInv.showInventory(p);
	}

}
