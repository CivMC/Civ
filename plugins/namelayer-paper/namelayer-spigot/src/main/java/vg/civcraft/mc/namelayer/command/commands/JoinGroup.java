package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class JoinGroup extends BaseCommandMiddle {

	@CommandAlias("nljg|join|joingroup")
	@Syntax("<group> <password>")
	@Description("Join a password protected group.")
	public void execute(Player sender, String groupName, String attemptedPassword) {
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disiplined.");
			return;
		}
		if (g.getPassword() == null){
			p.sendMessage(ChatColor.GREEN + "This group does not have a password, so you can't join it.");
			return;
		}
		if (!g.getPassword().equals(attemptedPassword)){
			p.sendMessage(ChatColor.RED + "That password is incorrect");
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		GroupPermission groupPerm = gm.getPermissionforGroup(g);
		PlayerType pType = groupPerm.getFirstWithPerm(PermissionType.getPermission("JOIN_PASSWORD"));
		if (pType == null){
			p.sendMessage(ChatColor.RED + "Someone derped. This group does not have the specified permission to let you join, sorry.");
			return;
		}
		if (g.isCurrentMember(uuid)){
			p.sendMessage(ChatColor.RED + "You are already a member.");
			return;
		}
		if(NameLayerPlugin.getBlackList().isBlacklisted(g, uuid)) {
			p.sendMessage(ChatColor.RED + "You can not join a group you have been blacklisted from");
			return;
		}

		g.addMember(uuid, pType);
		p.sendMessage(ChatColor.GREEN + "You have successfully been added to this group.");
	}
}
