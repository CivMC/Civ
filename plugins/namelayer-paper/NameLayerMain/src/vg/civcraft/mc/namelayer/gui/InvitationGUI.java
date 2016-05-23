package vg.civcraft.mc.namelayer.gui;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

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
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;
import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.misc.Mercury;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitationGUI extends GroupGUI{
	
	private PlayerType selectedType;
	private MemberViewGUI parent;
	
	public InvitationGUI(Group g, Player p, MemberViewGUI parent) {
		super(g,p);
		this.parent = parent;
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
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		ISUtils.setName(is, ChatColor.GOLD + "Invite as " + niceRankName);
		Clickable c;
		if (gm.hasAccess(g, p.getUniqueId(), perm)) {
			c = new Clickable(is) {
				
				@Override
				public void clicked(Player arg0) {
					selectedType = pType;
					Dialog enterName = new Dialog(arg0, NameLayerPlugin.getInstance()) {
						public void onReply(String [] message) {
							if (gm.hasAccess(g, p.getUniqueId(), MemberViewGUI.getAccordingPermission(selectedType))) {
								for(String s : message) {
									p.sendRawMessage(s);
									UUID inviteUUID = NameAPI.getUUID(s);
									if (inviteUUID == null) {
										p.sendRawMessage(ChatColor.RED + "The player " + s + " doesn't exist");
										continue;
									}
									if (g.isMember(inviteUUID)) { // So a player can't demote someone who is above them.
										p.sendRawMessage(ChatColor.RED + s +" is already a member of " + g.getName());
										continue;
									}
									if(NameLayerPlugin.getBlackList().isBlacklisted(g, inviteUUID)) {
										p.sendRawMessage(ChatColor.RED + s + " is currently blacklisted, you have to unblacklist him before inviting him to the group");
										continue;
									}
									InvitePlayer.sendInvitation(g, pType, inviteUUID, p.getUniqueId(), true);
									
									if(NameLayerPlugin.isMercuryEnabled()){
										MercuryAPI.sendGlobalMessage("addInvitation " + g.getGroupId() + " " + pType.toString() + " " + inviteUUID, "namelayer");
									}
									p.sendMessage(ChatColor.GREEN  + "Invited " + s + " as " + MemberViewGUI.getDirectRankName(pType));
								}
							}
							else {
								p.sendMessage(ChatColor.RED + "You lost permission to invite a player to this rank");
							}
							parent.showScreen();
						}
						
						public List <String> onTabComplete(String word, String [] msg) {
							List <String> names;
							if (NameLayerPlugin.isMercuryEnabled()) {
								names = new LinkedList<String>(MercuryAPI.getAllPlayers());
							}
							else {
								names = new LinkedList<String>();
								for(Player p : Bukkit.getOnlinePlayers()) {
									names.add(p.getName());
								}
							}
							if (word.equals("")) {
								return names;
							}
							List <String> result = new LinkedList<String>();
							String comp = word.toLowerCase();
							for(String s : names) {
								if (s.toLowerCase().startsWith(comp)) {
									result.add(s);
								}
							}
							return result;
						}
					};
				}
			};
		}
		else {
			ISUtils.addLore(is, ChatColor.RED + "You don't have permission to invite " + niceRankName);
			c = new DecorationStack(is);
		}
		return c;
	}
}
