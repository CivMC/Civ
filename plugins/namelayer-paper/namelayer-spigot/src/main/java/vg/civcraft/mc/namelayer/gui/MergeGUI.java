package vg.civcraft.mc.namelayer.gui;

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
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class MergeGUI extends AbstractGroupGUI {

	private AdminFunctionsGUI parent;
	private boolean mergeIntoThisGroup;
	private int currentPage;

	public MergeGUI(Group g, Player p, AdminFunctionsGUI parent) {
		super(g, p);
		this.parent = parent;
		this.currentPage = 0;
	}

	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		ItemStack mergeThisIntoOtherStack = new ItemStack(Material.MINECART);
		ItemUtils.setDisplayName(mergeThisIntoOtherStack, ChatColor.GOLD + "Merge this group into a different one");
		ItemUtils.addLore(mergeThisIntoOtherStack, ChatColor.AQUA
				+ "This action will transfer all members, reinforcements, snitches of this group to the one you chose next. "
				+ "This group will be deleted in the process");
		ItemStack mergeOtherIntoThisStack = mergeStack();
		ItemUtils.setDisplayName(mergeOtherIntoThisStack, ChatColor.GOLD + "Merge a different group into this one");
		ItemUtils.addLore(mergeOtherIntoThisStack, ChatColor.AQUA
				+ "This action will transfer all members, reinforcements and snitches of the group you chose next to this group. "
				+ "The group chosen will be deleted in the process");
		ci.setSlot(new Clickable(mergeOtherIntoThisStack) {

			@Override
			public void clicked(Player arg0) {
				mergeIntoThisGroup = true;
				showMergeGroupSelector();
			}
		}, 11);
		ci.setSlot(new Clickable(mergeThisIntoOtherStack) {

			@Override
			public void clicked(Player arg0) {
				mergeIntoThisGroup = false;
				showMergeGroupSelector();
			}
		}, 15);
		// exit button
		ItemStack backToOverview = goBackStack();
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Go back to previous menu");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 22);
		ci.showInventory(p);
	}

	private void showMergeGroupSelector() {
		ClickableInventory ci = new ClickableInventory(54, g.getName());
		final List<String> gName = gm.getAllGroupNames(p.getUniqueId());
		if (gName.size() < 45 * currentPage) {
			// would show an empty page, so go to previous
			currentPage--;
			showScreen();
		}
		for (int i = 45 * currentPage; i < 45 * (currentPage + 1) && i < gName.size(); i++) {
			final String currentName = gName.get(i);
			if (!gm.hasAccess(currentName, p.getUniqueId(), PermissionType.getPermission("MERGE"))) {
				// dont show groups player cant merge
				continue;
			}
			if (currentName.equals(g.getName())) {
				// cant merge with itself
				continue;
			}
			ItemStack is = new ItemStack(Material.MAGMA_CREAM);
			ItemUtils.setDisplayName(is, ChatColor.GOLD + currentName);
			ci.setSlot(new Clickable(is) {

				@Override
				public void clicked(Player arg0) {
					requestConfirmation(currentName);
				}
			}, 9 + i - (45 * currentPage));
		}
		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showMergeGroupSelector();
				}
			};
			ci.setSlot(baCl, 45);
		}
		// next button
		if ((45 * (currentPage + 1)) <= gName.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((45 * (currentPage + 1)) <= gName.size()) {
						currentPage++;
					}
					showMergeGroupSelector();
				}
			};
			ci.setSlot(forCl, 53);
		}

		// exit button
		ItemStack backToOverview = goBackStack();
		ItemUtils.setDisplayName(backToOverview, ChatColor.GOLD + "Exit selection");
		ci.setSlot(new Clickable(backToOverview) {

			@Override
			public void clicked(Player arg0) {
				parent.showScreen();
			}
		}, 49);
		ci.showInventory(p);
	}

	private void requestConfirmation(final String groupName) {
		ClickableInventory confirmInv = new ClickableInventory(27, g.getName());
		String fromGroup = mergeIntoThisGroup ? groupName : g.getName();
		String targetGroup = mergeIntoThisGroup ? g.getName() : groupName;
		ItemStack info = new ItemStack(Material.PAPER);
		ItemUtils.setDisplayName(info, ChatColor.GOLD + "Merge group");
		ItemUtils.addLore(info, ChatColor.RED + "Are you sure that you want to merge " + fromGroup + " into "
				+ targetGroup + "? You can not undo this!");
		ItemUtils.addLore(info, ChatColor.AQUA + "This will transfer all members, reinforcements, snitches etc. from "
				+ fromGroup + " to " + targetGroup + ". " + fromGroup + " will be deleted in the process");
		ItemStack yes = yesStack();
		ItemUtils.setDisplayName(yes, ChatColor.GOLD + "Yes, merge " + fromGroup + " into " + targetGroup);
		ItemStack no = noStack();
		ItemUtils.setDisplayName(no, ChatColor.GOLD + "No, don't merge " + g.getName());
		confirmInv.setSlot(new Clickable(yes) {

			@Override
			public void clicked(Player p) {
				requestMerge(groupName);
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

	private void requestMerge(String mergeGroupName) {
		final Group otherGroup = GroupManager.getGroup(mergeGroupName);
		if (otherGroup == null) {
			p.sendMessage(ChatColor.RED + "Something went wrong, please try again");
			showScreen();
			return;
		}
		if (!gm.hasAccess(otherGroup, p.getUniqueId(), PermissionType.getPermission("MERGE"))) {
			p.sendMessage(ChatColor.RED + "You dont have permission to merge " + otherGroup.getName());
			showScreen();
			return;
		}
		if (!gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("MERGE"))) {
			p.sendMessage(ChatColor.RED + "You dont have permission to merge " + g.getName());
			showScreen();
			return;
		}
		if (g.isDisciplined() || otherGroup.isDisciplined()) {
			p.sendMessage(ChatColor.RED + "One of the groups is disciplined, merging them failed");
			showScreen();
			return;
		}
		if (mergeGroupName.equals(g.getName())) {
			p.sendMessage(ChatColor.RED + "You cant merge a group with itself");
			showScreen();
			return;
		}
		NameLayerPlugin.log(Level.INFO, p.getName() + " merged " + g.getName() + " and " + mergeGroupName + "via gui, "
				+ (mergeIntoThisGroup ? g.getName() : mergeGroupName) + " was the group merged into");
		try {
			if (mergeIntoThisGroup) {
				gm.mergeGroup(g, otherGroup);
				p.sendMessage(ChatColor.GREEN + "Successfully merged " + otherGroup.getName() + " into " + g.getName());
				parent.showScreen();
			} else {
				gm.mergeGroup(otherGroup, g);
				p.sendMessage(ChatColor.GREEN + "Successfully merged " + g.getName() + " into " + otherGroup.getName());
			}
		} catch (Exception e) {
			NameLayerPlugin.getInstance().getLogger().log(Level.SEVERE, "Group merging failed", e);
			p.sendMessage(ChatColor.GREEN + "Group merging may have failed.");
		}
		p.sendMessage(ChatColor.GREEN + "Group is under going merge.");
	}

}
