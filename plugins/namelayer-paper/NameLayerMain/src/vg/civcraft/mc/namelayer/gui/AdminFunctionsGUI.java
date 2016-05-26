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

public class AdminFunctionsGUI extends AbstractGroupGUI {
	
	private MainGroupGUI parent;
	
	public AdminFunctionsGUI(Player p, Group g, MainGroupGUI parent) {
		super(g, p);
		this.parent = parent;
	}
	
	public void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
		//linking
		ItemStack linkStack = new ItemStack(Material.GOLD_INGOT);
		ISUtils.setName(linkStack, ChatColor.GOLD + "Link group");
		Clickable linkClick;
		if (gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("LINKING"))) {
			linkClick = new Clickable(linkStack) {
				@Override
				public void clicked(Player p) {
					showLinkingMenu();					
				}
			};
		}
		else {
			ISUtils.addLore(linkStack, ChatColor.RED + "You don't have permission to do this");
			linkClick = new DecorationStack(linkStack);
		}
		ci.setSlot(linkClick, 10);
		//merging
		ItemStack mergeStack = new ItemStack(Material.SPONGE);
		ISUtils.setName(mergeStack, ChatColor.GOLD + "Merge group");
		Clickable mergeClick;
		if (gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("MERGE"))) {
			mergeClick = new Clickable(mergeStack) {
				@Override
				public void clicked(Player p) {
					showMergingMenu();					
				}
			};
		}
		else {
			ISUtils.addLore(mergeStack, ChatColor.RED + "You don't have permission to do this");
			mergeClick = new DecorationStack(mergeStack);
		}
		ci.setSlot(mergeClick, 12);
		//transferring group
		ItemStack transferStack = new ItemStack(Material.PACKED_ICE);
		ISUtils.setName(transferStack, ChatColor.GOLD + "Tranfer group to new primary owner");
		Clickable transferClick;
		if (gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("TRANSFER"))) {
			transferClick = new Clickable(transferStack) {
				@Override
				public void clicked(Player p) {
					showTransferingMenu();					
				}
			};
		}
		else {
			ISUtils.addLore(transferStack, ChatColor.RED + "You don't have permission to do this");
			transferClick = new DecorationStack(transferStack);
		}
		ci.setSlot(transferClick, 14);
		//deleting group
		ItemStack deletionStack = new ItemStack(Material.BARRIER);
		ISUtils.setName(deletionStack, ChatColor.GOLD + "Tranfer group to new primary owner");
		Clickable deletionClick;
		if (gm.hasAccess(g, p.getUniqueId(), PermissionType.getPermission("DELETE"))) {
			deletionClick = new Clickable(deletionStack) {
				@Override
				public void clicked(Player p) {
					showDeletionMenu();					
				}
			};
		}
		else {
			ISUtils.addLore(deletionStack, ChatColor.RED + "You don't have permission to do this");
			deletionClick = new DecorationStack(deletionStack);
		}
		ci.setSlot(deletionClick, 16);
		
		//back button
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
		
	}
	
	private void showMergingMenu() {
		
	}
	
	private void showTransferingMenu() {
		
	}
	
	private void showDeletionMenu() {
		
	}

}
