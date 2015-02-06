package vg.civcraft.mc.namelayer.command.commands;

import java.util.ArrayList;
import java.util.List;
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

public class InfoDump extends PlayerCommand
{
	public InfoDump(String name)
	{
		super(name);
		setIdentifier("nlid");
		setDescription("This command dumps group info for CitadelGUI.");
		setUsage("/nlid");
		setArguments(0, 0);
		
		System.out.println("IT IS CALLED");
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "You are not a player?");
			return true;
		}
		
		Player player = (Player)sender;
		
		List<String> groupNames = gm.getAllGroupNames(player.getUniqueId());
		
		for(String groupName : groupNames)
		{
			Group group = gm.getGroup(groupName);
			GroupPermission permissions = gm.getPermissionforGroup(group);
			
			
			String output = "[NLID]: [GROUPNAME] " + group.getName() + " : [MEMBERSHIPLEVEL] " + group.getPlayerType(player.getUniqueId()) + " : [PERMS] " + permissions.listPermsforPlayerType(group.getPlayerType(player.getUniqueId()));
			
			output += " : [OWNERS]";
			for(UUID playerUUID : group.getAllMembers(PlayerType.OWNER))
			{
				output += " " + NameAPI.getCurrentName(playerUUID);
			}
			
			output += " : [ADMINS]";
			for(UUID playerUUID : group.getAllMembers(PlayerType.ADMINS))
			{
				output += " " + NameAPI.getCurrentName(playerUUID);
			}
			
			output += " : [MODS]";
			for(UUID playerUUID : group.getAllMembers(PlayerType.MODS))
			{
				output += " " + NameAPI.getCurrentName(playerUUID);
			}
			
			output += " : [MEMBERS]";
			for(UUID playerUUID : group.getAllMembers(PlayerType.MEMBERS))
			{
				output += " " + NameAPI.getCurrentName(playerUUID);
			}
			
			if(permissions.isAccessible(group.getPlayerType(player.getUniqueId()), PermissionType.LIST_PERMS))
			{
				output += " : [OWNER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.OWNER);
				output += " : [ADMIN-PERMS] " + permissions.listPermsforPlayerType(PlayerType.ADMINS);
				output += " : [MOD-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MODS);
				output += " : [MEMBER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MEMBERS);
			}
			
			player.sendMessage(ChatColor.GREEN + output);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) 
	{
		return null;
	}
}
