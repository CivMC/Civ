package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ModifyPermissions extends BaseCommandMiddle {

	@CommandAlias("nlmp|editperms|modifyperms")
	@Syntax("<group> <add/remove> <rank> <permission>")
	@Description("Modify the permissions of a group.")
	@CommandCompletion("@NL_Groups add|remove @NL_Ranks @NL_Perms")
	public void execute(CommandSender sender, String groupName, String adding, String playerRank, String permissionName) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "You must be a player. Nuf said.");
			return;
		}
		Player p = (Player) sender;
		Group g = GroupManager.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = g.getPlayerType(uuid);
		if (type == null){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is currently disiplined.");
			return;
		}
		if (!gm.hasAccess(g, uuid, PermissionType.getPermission("PERMS")) && !g.isOwner(uuid) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			return;
		}
		String info = adding;
		PlayerType playerType = PlayerType.getPlayerType(playerRank.toUpperCase());
		if (playerType == null){
			PlayerType.displayPlayerTypes(p);
			return;
		}
		PermissionType pType = PermissionType.getPermission(permissionName);
		if (pType == null){
			StringBuilder sb = new StringBuilder();
			for(PermissionType perm : PermissionType.getAllPermissions()) {
				sb.append(perm.getName());
				sb.append(" ");
			}
			p.sendMessage(ChatColor.RED 
						+ "That PermissionType does not exist.\n"
						+ "The current types are: " + sb.toString());
			return;
		}
		GroupPermission gPerm = gm.getPermissionforGroup(g);

		if (playerType == PlayerType.NOT_BLACKLISTED && !pType.getCanBeBlacklisted()) {
			sender.sendMessage(ChatColor.RED + "You can not change this permission for non-blacklisted players.");
			return;
		}

		if (info.equalsIgnoreCase("add")){
			if (gPerm.hasPermission(playerType, pType))
				sender.sendMessage(ChatColor.RED + "This PlayerType already has the PermissionType: " + pType.getName());
			else {
				if (playerType == PlayerType.NOT_BLACKLISTED && pType == PermissionType.getPermission("JOIN_PASSWORD")) {
					//we need to prevent players from explicitly adding people to this permission group
					sender.sendMessage(ChatColor.RED + "You can't explicitly add players to this group. Per default any non blacklisted person will"
							+ "be included in this permission group");
				}
				gPerm.addPermission(playerType, pType);
				sender.sendMessage(ChatColor.GREEN + "The PermissionType: " + pType.getName() + " was successfully added to the PlayerType: " +
						playerType.name());
			}
		}
		else if (info.equalsIgnoreCase("remove")){
			if (gPerm.hasPermission(playerType, pType)){
				gPerm.removePermission(playerType, pType);
				sender.sendMessage(ChatColor.GREEN + "The PermissionType: " + pType.getName() + " was successfully removed from" +
						" the PlayerType: " + playerType.name());
			}
			else
				sender.sendMessage(ChatColor.RED + "This PlayerType does not have the PermissionType: " + pType.getName());
		}
		else{
			p.sendMessage(ChatColor.RED + "Specify if you want to add or remove.");
		}
	}
}
