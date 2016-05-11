package vg.civcraft.mc.namelayer.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameLayerGUI extends GroupGUI {

	public NameLayerGUI(Player p, Group g) {
		super(g, p);
	}

	public void showInitialScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		ItemStack members = new ItemStack(Material.POTATO_ITEM);
		ISUtils.setName(members, ChatColor.GOLD + "Manage group members");
		ISUtils.addLore(members, ChatColor.AQUA + "Click here to:",
				ChatColor.GREEN + "Invite members", ChatColor.GREEN
						+ "View pending invites", ChatColor.GREEN
						+ "Remove members", ChatColor.GREEN
						+ "View existing members");
		Clickable memberClick = new Clickable(members) {

			@Override
			public void clicked(Player arg0) {
				showMemberManageScreen();
			}
		};
		ci.setSlot(memberClick, 10);

		ItemStack permissions = new ItemStack(Material.BOOK);
		ISUtils.setName(permissions, ChatColor.GOLD
				+ "Manage group permissions");
		ISUtils.addLore(permissions, ChatColor.AQUA + "Click here to:",
				ChatColor.GREEN + "Promote players", ChatColor.GREEN
						+ "Modify group permissions", ChatColor.GREEN
						+ "View current group permissions");

		Clickable permClick = new Clickable(permissions) {

			@Override
			public void clicked(Player arg0) {
				showPermissionManageScreen();
			}
		};
		ci.setSlot(permClick, 13);

		ItemStack modif = new ItemStack(Material.EMPTY_MAP);
		ISUtils.setName(modif, ChatColor.GOLD + "Modify group");
		ISUtils.addLore(modif, ChatColor.AQUA + "Click here to:",
				ChatColor.GREEN + "Delete the group", ChatColor.GREEN
						+ "Merge the group", ChatColor.GREEN
						+ "Transfer the group");
		Clickable modiClick = new Clickable(modif) {

			@Override
			public void clicked(Player arg0) {
				showModificationScreen();
			}
		};
		ci.setSlot(modiClick, 16);
		ci.showInventory(p);
	}

	public void showMemberManageScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		boolean canChangeMembers = hasOnePermission(
				PermissionType.getPermission("MEMBERS"),
				PermissionType.getPermission("MODS"),
				PermissionType.getPermission("ADMINS"),
				PermissionType.getPermission("OWNER"));

		ItemStack inv = new ItemStack(Material.PAPER);
		ISUtils.setName(inv, ChatColor.GOLD + "Invite members");
		Clickable invClick;
		if (canChangeMembers) {
			invClick = new Clickable(inv) {

				@Override
				public void clicked(Player arg0) {
					showInvitationScreen();
				}
			};
		} else {
			ISUtils.addLore(inv, ChatColor.RED
					+ "You don't have permission to do this");
			invClick = new DecorationStack(inv);
		}
		ci.setSlot(invClick, 10);

		ItemStack viewInvites = new ItemStack(Material.FENCE_GATE);
		ISUtils.setName(viewInvites, ChatColor.GOLD + "Show pending invites");
		Clickable pendInClick;
		if (canChangeMembers) {
			pendInClick = new Clickable(viewInvites) {

				@Override
				public void clicked(Player arg0) {
					showPendingInvitesScreen();
				}
			};
		} else {
			ISUtils.addLore(viewInvites, ChatColor.RED
					+ "You don't have permission to do this");
			pendInClick = new DecorationStack(viewInvites);
		}
		ci.setSlot(pendInClick, 13);

		ItemStack listMem = new ItemStack(Material.EMPTY_MAP);
		ISUtils.setName(listMem, ChatColor.GOLD + "View or remove group members");
		Clickable listClick;
		if (hasOnePermission(PermissionType.getPermission("GROUPSTATS"),
				PermissionType.getPermission("MEMBERS"),
				PermissionType.getPermission("MODS"),
				PermissionType.getPermission("ADMINS"),
				PermissionType.getPermission("OWNER"))) {
			listClick = new Clickable(listMem) {

				@Override
				public void clicked(Player arg0) {
					showPlayerListScreen();
				}
			};
		} else {
			ISUtils.addLore(listMem, ChatColor.RED
					+ "You don't have permission to do this");
			listClick = new DecorationStack(listMem);
		}
		ci.setSlot(listClick, 16);
		ItemStack goBack = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(goBack, ChatColor.GOLD + "Go back to group overview");
		ci.setSlot(new Clickable(goBack) {

			@Override
			public void clicked(Player arg0) {
				showInitialScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

	private void showPermissionManageScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		
		ItemStack perms = new ItemStack(Material.BOOK);
		ISUtils.setName(perms, ChatColor.GOLD + "Modify group permissions");
		Clickable per;
		
		notImplemented();
		
	}

	private void showModificationScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		notImplemented();
	}

	private void showInvitationScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		notImplemented();

	}

	private void showPlayerListScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		new MemberViewGUI(p, g, this); // will handle everything on it's own
	}

	private void showPendingInvitesScreen() {
		ClickableInventory.forceCloseInventory(p);
		if (!validGroup()) {
			return;
		}
		new InvitationViewGUI(p, g, this); // will handle everything on it's own
	}

	private boolean hasOnePermission(PermissionType... perm) {
		for (PermissionType per : perm) {
			if (gm.hasAccess(g, p.getUniqueId(), per)) {
				return true;
			}
		}
		return false;
	}
	
	private void notImplemented() {
		p.sendMessage(ChatColor.RED + "Not implemented yet");
	}
}
