package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nlid")
@CommandPermission("namelayer.admin")
public class InfoDump extends BaseCommandMiddle {
	
	@Syntax("/nlid (page)")
	@Description("This command dumps group info for CitadelGUI.")
	public void execute(CommandSender sender, @Optional String groupID) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You are not a player?");
			return;
		}
		
		Player player = (Player)sender;
		UUID playerUUID = NameAPI.getUUID(player.getName());
		
		List<String> groupNames = gm.getAllGroupNames(player.getUniqueId());
		
		if(groupID == null) {
			player.sendMessage(ChatColor.GREEN + "[NLID]: " + groupNames.size());
			return;
		} else {
			
			int page = 0;
			try {
				page = Integer.parseInt(groupID);
			}
			catch(Exception e) {
				player.sendMessage(ChatColor.RED + "Please enter a valid number");
				return;
			}

			Group group;
			try {
				group = gm.getGroup(groupNames.get(page-1));
			}
			catch(Exception e) {
				player.sendMessage(ChatColor.RED + "No such Group");
				return;
			}
			GroupPermission permissions = gm.getPermissionforGroup(group);
			StringBuilder outputBuilder = new StringBuilder();
			outputBuilder.append("[NLID] : [GROUPNAME] ");
			outputBuilder.append(group.getName());
			outputBuilder.append(" : [MEMBERSHIPLEVEL] ");
			outputBuilder.append(group.getPlayerType(playerUUID));
			outputBuilder.append(" : [PERMS] ");
			outputBuilder.append(permissions.listPermsforPlayerType(group.getPlayerType(playerUUID)));
			

			outputBuilder.append(" : [OWNERS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("OWNER"))) {
				for(UUID ownerUUID : group.getAllMembers(PlayerType.OWNER)) {
					outputBuilder.append(" " + NameAPI.getCurrentName(ownerUUID));
				}
			} else {
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.OWNER).size());
			}

			outputBuilder.append(" : [ADMINS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("ADMINS"))) {
				for(UUID adminUUID : group.getAllMembers(PlayerType.ADMINS)) {
					outputBuilder.append(" " + NameAPI.getCurrentName(adminUUID));
				}
			} else {
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.ADMINS).size());
			}

			outputBuilder.append(" : [MODS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("MODS"))) {
				for(UUID modUUID : group.getAllMembers(PlayerType.MODS)) {
					outputBuilder.append(" " + NameAPI.getCurrentName(modUUID));
				}
			} else {
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.MODS).size());
			}

			outputBuilder.append(" : [MEMBERS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("MEMBERS"))) {
				for(UUID memberUUID : group.getAllMembers(PlayerType.MEMBERS)) {
					outputBuilder.append(" " + NameAPI.getCurrentName(memberUUID));
				}
			} else {
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.MEMBERS).size());
			}

			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("LIST_PERMS"))) {
				outputBuilder.append(" : [OWNER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.OWNER));
				outputBuilder.append(" : [ADMIN-PERMS] " + permissions.listPermsforPlayerType(PlayerType.ADMINS));
				outputBuilder.append(" : [MOD-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MODS));
				outputBuilder.append(" : [MEMBER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MEMBERS));
			}

			player.sendMessage(ChatColor.GREEN + outputBuilder.toString());
		}
	}
}
