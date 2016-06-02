package vg.civcraft.mc.namelayer.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
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
		ISUtils.setName(linkStack, ChatColor.GOLD + "Link group");
		Clickable linkClick;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("LINKING"))) {
			linkClick = new Clickable(linkStack) {
				@Override
				public void clicked(Player p) {
					showLinkingMenu();
				}
			};
		} else {
			ISUtils.addLore(linkStack, ChatColor.RED
					+ "You don't have permission to do this");
			linkClick = new DecorationStack(linkStack);
		}
		ci.setSlot(linkClick, 10);
		// merging
		ItemStack mergeStack = new ItemStack(Material.SPONGE);
		ISUtils.setName(mergeStack, ChatColor.GOLD + "Merge group");
		Clickable mergeClick;
		if (gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("MERGE"))) {
			mergeClick = new Clickable(mergeStack) {
				@Override
				public void clicked(Player p) {
					showMergingMenu();
				}
			};
		} else {
			ISUtils.addLore(mergeStack, ChatColor.RED
					+ "You don't have permission to do this");
			mergeClick = new DecorationStack(mergeStack);
		}
		ci.setSlot(mergeClick, 12);
		// transferring group
		ItemStack transferStack = new ItemStack(Material.PACKED_ICE);
		ISUtils.setName(transferStack, ChatColor.GOLD
				+ "Tranfer group to new primary owner");
		Clickable transferClick;
		if (g.isOwner(p.getUniqueId())) {
			transferClick = new Clickable(transferStack) {
				@Override
				public void clicked(Player p) {
					showTransferingMenu();
				}
			};
		} else {
			ISUtils.addLore(transferStack, ChatColor.RED
					+ "You don't have permission to do this");
			transferClick = new DecorationStack(transferStack);
		}
		ci.setSlot(transferClick, 14);
		// deleting group
		ItemStack deletionStack = new ItemStack(Material.BARRIER);
		ISUtils.setName(deletionStack, ChatColor.GOLD + "Delete group");
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
			ISUtils.addLore(deletionStack, ChatColor.RED
					+ "You don't have permission to do this");
			deletionClick = new DecorationStack(deletionStack);
		}
		ci.setSlot(deletionClick, 16);

		// back button
		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Back to overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

	private void showLinkingMenu() {
		LinkingGUI lgui = new LinkingGUI(g, p, this);
		lgui.showScreen();
	}

	private void showMergingMenu() {
		MergeGUI mGui = new MergeGUI(g, p, this);
		mGui.showScreen();
	}

	private void showTransferingMenu() {
		p.sendMessage(ChatColor.GOLD
				+ "Enter the name of the new primary owner or \"cancel\" to exit this prompt");
		Dialog dia = new Dialog(p, NameLayerPlugin.getInstance()) {

			@Override
			public List<String> onTabComplete(String word, String[] arg1) {
				List<String> names;
				if (NameLayerPlugin.isMercuryEnabled()) {
					names = new LinkedList<String>(MercuryAPI.getAllPlayers());
				} else {
					names = new LinkedList<String>();
					for (Player p : Bukkit.getOnlinePlayers()) {
						names.add(p.getName());
					}
				}
				if (word.equals("")) {
					return names;
				}
				List<String> result = new LinkedList<String>();
				String comp = word.toLowerCase();
				for (String s : names) {
					if (s.toLowerCase().startsWith(comp)) {
						result.add(s);
					}
				}
				return result;
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
				ISUtils.setName(info, ChatColor.GOLD + "Transfer group to "
						+ playerName);
				ISUtils.addLore(info, ChatColor.RED
						+ "Are you sure that you want to", ChatColor.RED
						+ "transfer this group? You can not undo this!");
				ItemStack yes = new ItemStack(Material.INK_SACK);
				yes.setDurability((short) 10); // green
				ISUtils.setName(yes,
						ChatColor.GOLD + "Yes, transfer	 " + g.getName()
								+ " to " + playerName);
				ItemStack no = new ItemStack(Material.INK_SACK);
				no.setDurability((short) 1); // red
				ISUtils.setName(no,
						ChatColor.GOLD + "No, don't transfer " + g.getName());
				confirmInv.setSlot(new Clickable(yes) {

					@Override
					public void clicked(Player p) {
						if (!g.isOwner(p.getUniqueId())) {
							p.sendMessage(ChatColor.RED
									+ "You are no longer the owner of this group. What happened?");
							showScreen();
							return;
						}
						if (g.isDisciplined()) {
							p.sendMessage(ChatColor.RED
									+ "This group is disciplined.");
							showScreen();
							return;
						}
						if (NameLayerPlugin.getInstance().getGroupLimit() < gm
								.countGroups(transferUUID) + 1) {
							p.sendMessage(ChatColor.RED
									+ playerName
									+ " cannot receive the group! This player has already reached the group limit count.");
							showScreen();
							return;
						}
						if (!g.isMember(transferUUID)) {
							p.sendMessage(ChatColor.RED
									+ playerName
									+ " is not a member of the group and can't be made primary owner!");
							showScreen();
							return;
						}
						NameLayerPlugin.log(Level.INFO,
								p.getName() + " transferred group to "
										+ NameAPI.getCurrentName(transferUUID)
										+ " for group " + g.getName()
										+ "via gui");
						g.setOwner(transferUUID);
						p.sendMessage(ChatColor.GREEN + playerName
								+ " has been given ownership of the group.");
						checkRecacheGroup();
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
		ISUtils.setName(info, ChatColor.GOLD + "Delete group");
		ISUtils.addLore(info, ChatColor.RED + "Are you sure that you want to",
				ChatColor.RED + "delete this group? You can not undo this!");
		ItemStack yes = new ItemStack(Material.INK_SACK);
		yes.setDurability((short) 10); // green
		ISUtils.setName(yes, ChatColor.GOLD + "Yes, delete " + g.getName());
		ItemStack no = new ItemStack(Material.INK_SACK);
		no.setDurability((short) 1); // red
		ISUtils.setName(no, ChatColor.GOLD + "No, keep " + g.getName());
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
						p.getName() + " deleted " + g.getName() + "via gui");
				if (gm.deleteGroup(g.getName())) {
					p.sendMessage(ChatColor.GREEN + g.getName()
							+ " was successfully deleted.");
				} else {
					p.sendMessage(ChatColor.GREEN + "Group is now disciplined."
							+ " Check back later to see if group is deleted.");
				}
				checkRecacheGroup();
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
