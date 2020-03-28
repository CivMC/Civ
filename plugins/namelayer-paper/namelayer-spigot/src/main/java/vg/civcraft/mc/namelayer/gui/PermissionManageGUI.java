package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class PermissionManageGUI extends AbstractGroupGUI {

	private MainGroupGUI parent;
	private int currentPage;

	public PermissionManageGUI(Group g, Player p, MainGroupGUI parentGUI) {
		super(g, p);
		this.parent = parentGUI;
		currentPage = 0;
	}

	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		if (!validGroup()) {
			return;
		}
		//dye blacklisted clickable black
		Clickable blackClick = produceSelectionClickable(Material.LEATHER_CHESTPLATE, PlayerType.NOT_BLACKLISTED);
		ItemStack blackStack = blackClick.getItemStack();
		LeatherArmorMeta meta = (LeatherArmorMeta)blackStack.getItemMeta();
		meta.setColor(Color.BLACK);
		blackStack.setItemMeta(meta);
		ci.setSlot(blackClick, 9);
		ci.setSlot(
				produceSelectionClickable(Material.LEATHER_CHESTPLATE,
						PlayerType.MEMBERS), 11);
		ci.setSlot(
				produceSelectionClickable(modMat(),
						PlayerType.MODS), 13);
		ci.setSlot(
				produceSelectionClickable(Material.IRON_CHESTPLATE,
						PlayerType.ADMINS), 15);
		ci.setSlot(
				produceSelectionClickable(Material.DIAMOND_CHESTPLATE,
						PlayerType.OWNER), 17);
		ItemStack backStack = new ItemStack(Material.ARROW);
		ItemAPI.setDisplayName(backStack, ChatColor.GOLD
				+ "Go back to member management");
		ci.setSlot(new Clickable(backStack) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

	private Clickable produceSelectionClickable(Material mat,
			final PlayerType pType) {
		ItemStack is = new ItemStack(mat);
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		Clickable c;
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "View and edit permissions for "
				+ PlayerType.getNiceRankName(pType));
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("LIST_PERMS"))) {
			ItemAPI.addLore(is, ChatColor.RED + "You are not allowed to list",
					ChatColor.RED + "permissions for this group");
			c = new DecorationStack(is);
		} else {
			c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					showPermissionEditing(pType);
				}
			};
		}
		return c;
	}

	private void showPermissionEditing(final PlayerType pType) {
		if (!validGroup()) {
			return;
		}
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("LIST_PERMS"))) {
			p.sendMessage(ChatColor.RED
					+ "You are not allowed to list permissions for this group");
			showScreen();
			return;
		}
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<Clickable> clicks = new ArrayList<Clickable>();
		final GroupPermission gp = gm.getPermissionforGroup(g);
		for (final PermissionType perm : PermissionType.getAllPermissions()) {
			ItemStack is = null;
			Clickable c;
			final boolean hasPerm = gp.hasPermission(pType, perm);
			boolean canEdit = gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("PERMS"));

			if (hasPerm) {
				is = yesStack();
				ItemAPI.addLore(
						is,
						ChatColor.DARK_AQUA
								+ PlayerType.getNiceRankName(pType)
								+ "s currently have", ChatColor.DARK_AQUA
								+ "this permission");
			} else {
				is = noStack();
				ItemAPI.addLore(
						is,
						ChatColor.DARK_AQUA
								+ PlayerType.getNiceRankName(pType)
								+ "s currently don't have", ChatColor.DARK_AQUA
								+ "this permission");
			}
			ItemAPI.setDisplayName(is, perm.getName());
			String desc = perm.getDescription();
			if (desc != null) {
				ItemAPI.addLore(is, ChatColor.GREEN + desc);
			}

			if (pType == PlayerType.NOT_BLACKLISTED && !perm.getCanBeBlacklisted()) {
				canEdit = false;

				ItemAPI.addLore(
						is,
						ChatColor.AQUA
								+ "This permission cannot be toggled for "
								+ PlayerType.getNiceRankName(pType)
				);
			}

			if (canEdit) {
				ItemAPI.addLore(is, ChatColor.AQUA + "Click to toggle");
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						if (hasPerm == gp.hasPermission(pType, perm)) { // recheck
							if (gm.hasAccess(g, p.getUniqueId(),
									PermissionType.getPermission("PERMS"))) {
								NameLayerPlugin.log(Level.INFO, p.getName()
										+ (hasPerm ? " removed " : " added ")
										+ "the permission " + perm.getName()
										+ "for player type " + pType.toString()
										+ " for " + g.getName() + " via the gui");
								if (hasPerm) {
									gp.removePermission(pType, perm);
								} else {
									gp.addPermission(pType, perm);
								}
							}
						} else {
							p.sendMessage(ChatColor.RED
									+ "Something changed while you were modifying permissions, so cancelled the process");
						}
						showPermissionEditing(pType);
					}
				};
			} else {
				c = new DecorationStack(is);
			}
			clicks.add(c);
		}

		if (clicks.size() < 45 * currentPage) {
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < clicks.size(); i++) {
			ci.setSlot(clicks.get(i), i - (45 * currentPage));
		}

		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemAPI.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (currentPage + 1)) <= clicks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemAPI.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= clicks.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		ItemStack backToOverview = goBackStack(); 
		ItemAPI.setDisplayName(backToOverview, ChatColor.GOLD + "Go back");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				showScreen();
			}
		}, 49);
		ci.showInventory(p);
	}

}
