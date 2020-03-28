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
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.bukkit.util.StringUtil;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.chatDialog.Dialog;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.DecorationStack;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.commands.InvitePlayer;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InvitationGUI extends AbstractGroupGUI{
	
	private PlayerType selectedType;
	private MainGroupGUI parent;
	
	public InvitationGUI(Group g, Player p, MainGroupGUI parent) {
		super(g,p);
		this.parent = parent;
		showScreen();
	}
	
	private void showScreen() {
		ClickableInventory ci = new ClickableInventory(27, g.getName());
	
		ItemStack explain = new ItemStack(Material.PAPER);
		ItemAPI.setDisplayName(explain, ChatColor.GOLD + "Select an option");
		ItemAPI.addLore(explain, ChatColor.AQUA + "Please select the rank ", ChatColor.AQUA + "you want the invited player to have");
		ci.setSlot(new DecorationStack(explain), 4);
		ci.setSlot(produceOptionStack(Material.LEATHER_CHESTPLATE, "member", PlayerType.MEMBERS, PermissionType.getPermission("MEMBERS")), 10);
		ci.setSlot(produceOptionStack(modMat(), "mod", PlayerType.MODS, PermissionType.getPermission("MODS")), 12);
		ci.setSlot(produceOptionStack(Material.IRON_CHESTPLATE, "admin", PlayerType.ADMINS, PermissionType.getPermission("ADMINS")), 14);
		ci.setSlot(produceOptionStack(Material.DIAMOND_CHESTPLATE, "owner", PlayerType.OWNER, PermissionType.getPermission("OWNER")), 16);
		ci.showInventory(p);
	}
	
	private Clickable produceOptionStack(Material item, String niceRankName, final PlayerType pType, PermissionType perm) {
		ItemStack is = new ItemStack(item);
		ItemMeta im = is.getItemMeta();
		im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		is.setItemMeta(im);
		ItemAPI.setDisplayName(is, ChatColor.GOLD + "Invite as " + niceRankName);
		Clickable c;
		if (gm.hasAccess(g, p.getUniqueId(), perm)) {
			c = new Clickable(is) {
				
				@Override
				public void clicked(Player arg0) {
					p.sendMessage(ChatColor.GOLD + "Enter the name of the player to invite or \"cancel\" to exit this prompt. You may also enter the names "
							+ "of multiple players, separated with spaces to invite all of them.");
					selectedType = pType;
					ClickableInventory.forceCloseInventory(arg0);
					new Dialog(arg0, NameLayerPlugin.getInstance()) {
						public void onReply(String [] message) {
							if (gm.hasAccess(g, p.getUniqueId(), MainGroupGUI.getAccordingPermission(selectedType))) {
								for(String s : message) {
									if (s.equalsIgnoreCase("cancel")) {
										parent.showScreen();
										return;
									}
									UUID inviteUUID = NameAPI.getUUID(s);
									if (inviteUUID == null) {
										p.sendMessage(ChatColor.RED + "The player " + s + " doesn't exist");
										continue;
									}
									if (g.isMember(inviteUUID)) { // So a player can't demote someone who is above them.
										p.sendMessage(ChatColor.RED + NameAPI.getCurrentName(inviteUUID) + " is already a member of " + g.getName());
										continue;
									}
									if(NameLayerPlugin.getBlackList().isBlacklisted(g, inviteUUID)) {
										p.sendMessage(ChatColor.RED + NameAPI.getCurrentName(inviteUUID) + " is currently blacklisted, you have to unblacklist him before inviting him to the group");
										continue;
									}
									NameLayerPlugin.log(Level.INFO,
											p.getName() + " invited "
													+ NameAPI.getCurrentName(inviteUUID)
													+ " to group " + g.getName()
													+ " via the gui");

									InvitePlayer.sendInvitation(g, pType, inviteUUID, p.getUniqueId(), true);

									p.sendMessage(ChatColor.GREEN  + "Invited " + NameAPI.getCurrentName(inviteUUID) + " as " + PlayerType.getNiceRankName(pType));
								}
							} else {
								p.sendMessage(ChatColor.RED + "You do not have permission to invite a player to this rank");
							}
							parent.showScreen();
						}
						
						public List<String> onTabComplete(String word, String[] msg) {
							List<String> players = Bukkit.getOnlinePlayers().stream()
									.filter(p -> !g.isMember(p.getUniqueId()))
									.map(Player::getName)
									.collect(Collectors.toList());
							players.add("cancel");

							return StringUtil.copyPartialMatches(word, players, new ArrayList<>());
						}
					};
				}
			};
		} else {
			ItemAPI.addLore(is, ChatColor.RED + "You don't have permission to invite " + niceRankName);
			c = new DecorationStack(is);
		}
		return c;
	}
}
