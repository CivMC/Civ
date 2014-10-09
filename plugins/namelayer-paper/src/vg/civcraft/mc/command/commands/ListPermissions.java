package vg.civcraft.mc.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.GroupManager.PlayerType;
import vg.civcraft.mc.NameAPI;
import vg.civcraft.mc.command.PlayerCommand;
import vg.civcraft.mc.group.Group;
import vg.civcraft.mc.permission.GroupPermission;
import vg.civcraft.mc.permission.PermissionType;

public class ListPermissions extends PlayerCommand{

	public ListPermissions(String name) {
		super(name);
		setDescription("This command is used to show permissions for a PlayerType in a specific group.");
		setUsage("/groupslistpermissions <group> <PlayerType>");
		setIdentifier("groupslistpermissions");
		setArguments(2,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "No.");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (g == null){
			p.sendMessage(ChatColor.RED + "That group does not exist.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType playerType = g.getPlayerType(uuid);
		if (playerType == null){
			p.sendMessage(ChatColor.RED + "You do not have access to this group.");
			return true;
		}
		
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if (!gPerm.isAccessible(PermissionType.LIST_PERMS, playerType)){
			p.sendMessage(ChatColor.RED + "You do not have permission in this group to run this command.");
			return true;
		}
		
		PlayerType check = PlayerType.getPlayerType(args[1]);
		if (check == null){
			PlayerType.displayPlayerTypes(p);
			return true;
		}
		String types = gPerm.listPermsforPlayerType(check);
		p.sendMessage(ChatColor.GREEN + types);
		return true;
	}

}
