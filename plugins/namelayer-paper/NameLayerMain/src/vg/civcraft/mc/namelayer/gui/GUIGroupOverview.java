package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GUIGroupOverview {

	private int currentPage;
	private Player p;
	private static GroupManager gm;

	public GUIGroupOverview(Player p) {
		if (this.gm == null) {
			this.gm = NameAPI.getGroupManager();
		}
		this.p = p;
		this.currentPage = 0;
	}

	public void showScreen() {

		ClickableInventory ci = new ClickableInventory(54, p.getName()
				+ "'s groups");
		final List<Clickable> groups = getGroupClickables();
		if (groups.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}

		for (int i = 45 * currentPage; i < 45 * (currentPage + 1)
				&& i < groups.size(); i++) {
			ci.setSlot(groups.get(i), i - (45 * currentPage));
		}

		// previous button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ISUtils.setName(back, ChatColor.GOLD + "Go to previous page");
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
		if ((45 * (currentPage + 1)) <= groups.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= groups.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 53);
		}

		ItemStack backToOverview = new ItemStack(Material.WOOD_DOOR);
		ISUtils.setName(backToOverview, ChatColor.GOLD + "Close");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				// just let it close, dont do anything
			}
		}, 49);
		ci.showInventory(p);
	}

	private List<Clickable> getGroupClickables() {
		String defaultGroupName = gm.getDefaultGroup(p.getUniqueId());
		List<String> groupNames = gm.getAllGroupNames(p.getUniqueId());
		List<Clickable> result = new ArrayList<Clickable>();
		Set <String> alreadyProcessed = new HashSet<String>();
		for (String groupName : groupNames) {
			if (alreadyProcessed.contains(groupName)) {
				continue;
			}
			final Group g = gm.getGroup(groupName);
			if (g == null) {
				continue;
			}
			Clickable c;
			PlayerType pType = g.getPlayerType(p.getUniqueId());
			if (pType == null) {
				continue;
			}
			ItemStack is = null;
			switch (pType) {
			case MEMBERS:
				is = new ItemStack(Material.LEATHER_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Member");
				break;
			case MODS:
				is = new ItemStack(Material.GOLD_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Mod");
				break;
			case ADMINS:
				is = new ItemStack(Material.IRON_CHESTPLATE);
				ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Admin");
				break;
			case OWNER:
				is = new ItemStack(Material.DIAMOND_CHESTPLATE);
				if (g.isOwner(p.getUniqueId())) {
					ISUtils.addLore(is, ChatColor.AQUA
							+ "Your rank: Primary owner");
				} else {
					ISUtils.addLore(is, ChatColor.AQUA + "Your rank: Owner");
				}
				break;
			}
			if (is == null) {
				continue;
			}
			ItemMeta im = is.getItemMeta();
			im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
			if (g.getName().equals(defaultGroupName)) {
				ISUtils.addLore(is, ChatColor.DARK_AQUA + "Your current default group");
				im.addEnchant(Enchantment.DURABILITY, 1, true);
			}
			is.setItemMeta(im);
			if (gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("GROUPSTATS"))) {
				ISUtils.addLore(
						is,
						ChatColor.AQUA
								+ String.valueOf(g.getAllMembers().size())
								+ " member"
								+ (g.getAllMembers().size() > 1 ? "s" : ""));
			}
			ISUtils.setName(is, ChatColor.GOLD + g.getName());
			if (gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("OPEN_GUI"))) {
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						MemberViewGUI mgui = new MemberViewGUI(p, g);
					}
				};
			} else {
				ISUtils.addLore(is, ChatColor.RED
						+ "You aren't permitted to open", ChatColor.RED
						+ "the GUI for this group");
				c = new DecorationStack(is);
			}
			result.add(c);
			alreadyProcessed.add(groupName);
		}
		return result;
	}
}
