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

public class ModifyPermissions extends PlayerCommand{

	public ModifyPermissions(String name) {
		super(name);
		setDescription("This command is used to modify the permissions of a group.");
		setUsage("/nlmp <group> <add/remove> <PlayerType> <PermissionType>");
		setIdentifier("nlmp");
		setArguments(4,4);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "You must be a player. Nuf said.");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (g == null){
			p.sendMessage(ChatColor.RED + "This group does not exist.");
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType type = g.getPlayerType(uuid);
		if (type == null){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return true;
		}
		if (g.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is currently disiplined.");
			return true;
		}
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if (!gPerm.isAccessible(type, PermissionType.PERMS) || !g.isOwner(uuid)){
			p.sendMessage(ChatColor.RED + "You do not have permission for this command.");
			return true;
		}
		String info = args[1];
		PlayerType playerType = PlayerType.getPlayerType(args[2]);
		if (playerType == null){
			PlayerType.displayPlayerTypes(p);
			return true;
		}
		PermissionType pType = PermissionType.getPermissionType(args[3]);
		if (pType == null){
			PermissionType.displayPermissionTypes(p);
			return true;
		}
		
		if (info.equalsIgnoreCase("add")){
			if (gPerm.isAccessible(playerType, pType))
				sender.sendMessage(ChatColor.RED + "This PlayerType already has the PermissionType: " + pType.name());
			else {
				gPerm.addPermission(playerType, pType);
				sender.sendMessage(ChatColor.GREEN + "The PermissionType: " + pType.name() + " was successfully added to the PlayerType: " +
				playerType.name());
			}
		}
		else if (info.equalsIgnoreCase("remove")){
			if (gPerm.isAccessible(playerType, pType)){
				gPerm.removePermission(playerType, pType);
				sender.sendMessage(ChatColor.GREEN + "The PermissionType: " + pType.name() + " was successfully removed from" +
						" the PlayerType: " + playerType.name());
			}
			else
				sender.sendMessage(ChatColor.RED + "This PlayerType does not have the PermissionType: " + pType.name());
		}
		else{
			p.sendMessage(ChatColor.RED + "Specify if you want to add or remove.");
		}
		return true;
	}
}
