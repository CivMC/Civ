package vg.civcraft.mc.namelayer.gui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitationGUI extends GroupGUI{
	
	private PlayerType selectedType;
	
	public InvitationGUI(Group g, Player p) {
		super(g,p);
		showScreen();
	}
	
	private void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
	
		ItemStack explain = new ItemStack(Material.PAPER);
		ISUtils.setName(explain, ChatColor.GOLD + "Select an option");
		ISUtils.addLore(explain, ChatColor.AQUA + "Please select the rank ", ChatColor.AQUA + "you want the invited player to have");
		ci.setSlot(new DecorationStack(explain), 4);
		ci.setSlot(produceOptionStack(Material.LEATHER_CHESTPLATE, "member", PlayerType.MEMBERS, PermissionType.getPermission("MEMBERS")), 10);
		ci.setSlot(produceOptionStack(Material.GOLD_CHESTPLATE, "mod", PlayerType.MODS, PermissionType.getPermission("MODS")), 12);
		ci.setSlot(produceOptionStack(Material.IRON_CHESTPLATE, "admin", PlayerType.ADMINS, PermissionType.getPermission("ADMINS")), 14);
		ci.setSlot(produceOptionStack(Material.DIAMOND_CHESTPLATE, "owner", PlayerType.OWNER, PermissionType.getPermission("OWNER")), 16);
		ci.showInventory(p);
	}
	
	private Clickable produceOptionStack(Material item, String niceRankName, final PlayerType pType, PermissionType perm) {
		ItemStack is = new ItemStack(item);
		ISUtils.setName(is, ChatColor.GOLD + "Invite as " + niceRankName);
		Clickable c;
		if (gm.hasAccess(g, p.getUniqueId(), perm)) {
			c = new Clickable(is) {
				
				@Override
				public void clicked(Player arg0) {
					selectedType = pType;					
				}
			};
		}
		else {
			ISUtils.addLore(is, ChatColor.RED + "You don't have permission to invite " + niceRankName);
			c = new DecorationStack(is);
		}
		return c;
	}

	//TODO
}
