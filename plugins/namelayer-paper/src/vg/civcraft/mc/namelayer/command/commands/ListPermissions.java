package vg.civcraft.mc.namelayer.command.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ListPermissions extends PlayerCommand{

	public ListPermissions(String name) {
		super(name);
		setIdentifier("nllp");
		setDescription("This command is used to show permissions for a PlayerType in a specific group.");
		setUsage("/nllp <group> <PlayerType>");
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
		if (!gPerm.isAccessible(playerType, PermissionType.LIST_PERMS)){
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
