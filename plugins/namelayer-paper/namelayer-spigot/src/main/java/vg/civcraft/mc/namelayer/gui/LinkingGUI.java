package vg.civcraft.mc.namelayer.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class LinkingGUI extends AbstractGroupGUI {

	private AdminFunctionsGUI parent;
	private boolean makingSubGroup;
	private int linkSelectingPage;
	private int subGroupSelectingPage;

	public LinkingGUI(Group g, Player p, AdminFunctionsGUI parent) {
		super(g, p);
		this.parent = parent;
		subGroupSelectingPage = 0;
	}

	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		ci.setSlot(getInfoClickable(), 4);
		if (g.hasSuperGroup()) {
			ci.setSlot(getRemoveSuperClickable(), 3);
		} else {
			ci.setSlot(getAddSuperClickable(), 3);
		}
		ci.setSlot(getAddSubClickable(), 5);
		final List<Clickable> clicks = getSubClickables();
		for (int i = (45 * subGroupSelectingPage) + 9; i < (45 * (subGroupSelectingPage + 1)) + 9
				&& i < clicks.size(); i++) {
			ci.setSlot(clicks.get(i), i - (45 * subGroupSelectingPage));
		}

		// previous button
		if (subGroupSelectingPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (subGroupSelectingPage > 0) {
						subGroupSelectingPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, 0);
		}
		// next button
		if ((45 * (subGroupSelectingPage + 1)) <= clicks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (subGroupSelectingPage + 1)) <= clicks.size()) {
						subGroupSelectingPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, 8);
		}

		// back button
		ItemStack backToOverview = goBackStack(); 
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Back to overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 7);
		ci.showInventory(p);
	}

	private List<Clickable> getSubClickables() {
		List<Clickable> clicks = new ArrayList<Clickable>();
		for (final Group sub : g.getSubgroups()) {
			ItemStack is = new ItemStack(Material.MAGMA_CREAM);
			ItemUtils.setDisplayName(is, ChatColor.GOLD + sub.getName());
			ItemUtils.addLore(is, ChatColor.AQUA + "This group has "
					+ sub.getSubgroups().size() + "sub groups itself");
			ItemUtils.addLore(is, ChatColor.DARK_AQUA
					+ "Click to remove this sub group");
			Clickable c = new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					if (!gm.hasAccess(g, p.getUniqueId(),
							PermissionType.getPermission("LINKING"))) {
						p.sendMessage(ChatColor.RED
								+ "You dont have permission to unlink "
								+ g.getName());
						showScreen();
						return;
					}
					if (!gm.hasAccess(sub, p.getUniqueId(),
							PermissionType.getPermission("LINKING"))) {
						p.sendMessage(ChatColor.RED
								+ "You dont have permission to unlink "
								+ sub.getName());
						showScreen();
						return;
					}
					boolean success = Group.unlink(g, sub);
					String message;
					if (success) {
						message = ChatColor.GREEN + sub.getName()
								+ " is no longer a sub group of " + g.getName();
					} else {
						message = ChatColor.RED
								+ "Failed to unlink the groups, you should complain to an admin about this";
					}
					p.sendMessage(message);
					showScreen();

				}
			};
			clicks.add(c);
		}
		return clicks;
	}

	private Clickable getAddSubClickable() {
		ItemStack makeSuper = new ItemStack(Material.LEATHER);
		ItemUtils.setDisplayName(makeSuper, ChatColor.GOLD + "Add a new subgroup");
		ItemUtils.addLore(
				makeSuper,
				ChatColor.AQUA
						+ "This option means that the additional group you chose will inherit all members of "
						+ g.getName() + " with their ranks");
		Clickable superClick = new Clickable(makeSuper) {

			@Override
			public void clicked(Player arg0) {
				makingSubGroup = false;
				linkSelectingPage = 0;
				showGroupSelector();
			}
		};
		return superClick;
	}

	private Clickable getInfoClickable() {
		ItemStack is = new ItemStack(Material.PAPER);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Linking stats for " + g.getName());
		if (g.hasSuperGroup()) {
			ItemUtils.addLore(is, ChatColor.AQUA + "Current super group: "
					+ g.getSuperGroup().getName());
		} else {
			ItemUtils.addLore(is, ChatColor.AQUA + "No current super group");
		}
		ItemUtils.addLore(is, ChatColor.DARK_AQUA + "Currently "
				+ g.getSubgroups().size() + " sub group"
				+ ((g.getSubgroups().size() == 1) ? "" : "s")
				+ ", which are listed below");
		return new DecorationStack(is);
	}

	private Clickable getRemoveSuperClickable() {
		ItemStack is = new ItemStack(Material.DIAMOND);
		ItemUtils.setDisplayName(is, ChatColor.GOLD + "Remove current super group");
		ItemUtils.addLore(is, ChatColor.AQUA + g.getSuperGroup().getName()
				+ " is the super group of " + g.getName());
		ItemUtils.addLore(is, ChatColor.DARK_AQUA + "Click to remove this link");
		Clickable c = new Clickable(is) {

			@Override
			public void clicked(Player arg0) {
				if (!gm.hasAccess(g, p.getUniqueId(),
						PermissionType.getPermission("LINKING"))) {
					p.sendMessage(ChatColor.RED
							+ "You dont have permission to unlink "
							+ g.getName());
					showScreen();
					return;
				}
				if (!gm.hasAccess(g.getSuperGroup(), p.getUniqueId(),
						PermissionType.getPermission("LINKING"))) {
					p.sendMessage(ChatColor.RED
							+ "You dont have permission to unlink "
							+ g.getSuperGroup().getName());
					showScreen();
					return;
				}
				Group superGroup = g.getSuperGroup();
				boolean success = Group.unlink(superGroup, g);
				String message;
				if (success) {
					message = ChatColor.GREEN + g.getName()
							+ " is no longer a sub group of "
							+ superGroup.getName();
				} else {
					message = ChatColor.RED
							+ "Failed to unlink the groups, you should complain to an admin about this";
				}
				p.sendMessage(message);
				showScreen();
			}
		};
		return c;
	}

	private Clickable getAddSuperClickable() {
		ItemStack makeSub = new ItemStack(Material.BEACON);
		ItemUtils.setDisplayName(makeSub, ChatColor.GOLD + "Add super group");
		ItemUtils.addLore(
				makeSub,
				ChatColor.AQUA
						+ "This option means that "
						+ g.getName()
						+ " will inherit all members with their respective ranks from the second group you chose");
		Clickable subClick = new Clickable(makeSub) {

			@Override
			public void clicked(Player arg0) {
				makingSubGroup = true;
				linkSelectingPage = 0;
				showGroupSelector();
			}
		};
		return subClick;
	}

	private void showGroupSelector() {
		final List<Clickable> clicks = new ArrayList<Clickable>();
		for (final String groupName : gm.getAllGroupNames(p.getUniqueId())) {
			Group g = gm.getGroup(groupName);
			if (g == null) {
				// ????
				continue;
			}
			ItemStack is = new ItemStack(Material.MAGMA_CREAM);
			ItemUtils.setDisplayName(is, g.getName());
			Clickable c;
			if (!gm.hasAccess(g, p.getUniqueId(),
					PermissionType.getPermission("LINKING"))) {
				if (!makingSubGroup && g.hasSuperGroup()) {
					// making a supergroup, but this one already has one
					ItemUtils.addLore(is, ChatColor.RED
							+ "This group already has a super group");
				} else {
					ItemUtils.addLore(is, ChatColor.RED
							+ "You don't have permission to link this group");
				}
				c = new DecorationStack(is);
			} else {
				c = new Clickable(is) {

					@Override
					public void clicked(Player arg0) {
						requestLink(groupName);
						showScreen();
					}
				};
			}
			clicks.add(c);
		}
		ClickableInventory ci = new ClickableInventory(54, this.g.getName());
		if (clicks.size() < 45 * linkSelectingPage) {
			// would show an empty page, so go to previous
			linkSelectingPage--;
		}

		for (int i = 45 * linkSelectingPage; i < 45 * (linkSelectingPage + 1)
				&& i < clicks.size(); i++) {
			ci.setSlot(clicks.get(i), i - (45 * linkSelectingPage));
		}
		// previous button
		if (linkSelectingPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (linkSelectingPage > 0) {
						linkSelectingPage--;
					}
					showGroupSelector();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (linkSelectingPage + 1)) <= clicks.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (linkSelectingPage + 1)) <= clicks.size()) {
						linkSelectingPage++;
					}
					showGroupSelector();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// close button
		ItemStack backToOverview = goBackStack(); 
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Back to overview");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				showScreen();
			}
		}, 49);
		ci.showInventory(p);
	}

	private void requestLink(String groupName) {
		Group linkGroup = gm.getGroup(groupName);
		if (linkGroup == null) {
			p.sendMessage(ChatColor.RED
					+ "This group no longer exists? Something went wrong");
			showScreen();
			return;
		}
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED + "You dont have permission to link "
					+ g.getName());
			showScreen();
			return;
		}
		if (!gm.hasAccess(linkGroup, p.getUniqueId(),
				PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED + "You dont have permission to link "
					+ linkGroup.getName());
			showScreen();
			return;
		}
		if (makingSubGroup && g.hasSuperGroup()) {
			p.sendMessage(ChatColor.RED + g.getName()
					+ " already has a super group");
			showScreen();
			return;
		}
		if (!makingSubGroup && linkGroup.hasSuperGroup()) {
			p.sendMessage(ChatColor.RED + linkGroup.getName()
					+ " already has a super group");
			showScreen();
			return;
		}
		boolean linkCheck;
		if (makingSubGroup) {
			linkCheck = Group.areLinked(linkGroup, g);
		} else {
			linkCheck = Group.areLinked(g, linkGroup);
		}
		if (!linkCheck) {
			p.sendMessage(ChatColor.RED
					+ "Those groups are already linked directly or indirectly, you can't link them");
			showScreen();
			return;
		}
		if (g.isDisciplined() || linkGroup.isDisciplined()) {
			p.sendMessage(ChatColor.RED + "One of the groups is disciplined.");
			showScreen();
			return;
		}
		boolean success;
		if (makingSubGroup) {
			success = Group.link(linkGroup, g, true);
		} else {
			success = Group.link(g, linkGroup, true);
		}
		NameLayerPlugin.log(
				Level.INFO,
				p.getName()
						+ " linked "
						+ linkGroup.getName()
						+ " and "
						+ g.getName()
						+ " via the gui, "
						+ (makingSubGroup ? linkGroup.getName() : g.getName()
								+ " was the super group"));
		String message;
		if (success) {
			if (makingSubGroup) {
				message = ChatColor.GREEN + "Successfully made " + g.getName()
						+ " a subgroup of " + linkGroup.getName();
			} else {
				message = ChatColor.GREEN + "Successfully made "
						+ linkGroup.getName() + " a subgroup of " + g.getName();
			}
		} else {
			message = ChatColor.RED
					+ "Failed to link the groups, you should complain to an admin about this";
		}
		p.sendMessage(message);
	}

}
