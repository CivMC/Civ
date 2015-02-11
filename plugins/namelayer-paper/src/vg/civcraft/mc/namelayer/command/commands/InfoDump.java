package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
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
		setUsage("/nlid (page)");
		setArguments(0, 1);
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args)
	{
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "You are not a player?");
			return true;
		}
		
		Player player = (Player)sender;
		UUID playerUUID = NameAPI.getUUID(player.getName());
		
		List<String> groupNames = gm.getAllGroupNames(player.getUniqueId());
		
		if(args.length == 0)
		{
			player.sendMessage(ChatColor.GREEN + "[NLID]: " + groupNames.size());
			return true;
		}
		else
		{
			
			int page = 0;
			try
			{
				page = Integer.parseInt(args[0]);
			}
			catch(Exception e)
			{
				player.sendMessage(ChatColor.RED + "Please enter a valid number");
				return true;
			}

			Group group;
			try
			{
				group = gm.getGroup(groupNames.get(page-1));
			}
			catch(Exception e)
			{
				player.sendMessage(ChatColor.RED + "No such Group");
				return true;
			}
			PlayerType pType = group.getPlayerType(playerUUID);
			GroupPermission permissions = gm.getPermissionforGroup(group);
			if (!permissions.isAccessible(pType, PermissionType.GROUPSTATS) && !(player.isOp() || player.hasPermission("namelayer.admin"))){
				player.sendMessage(ChatColor.RED + "You do not have permission from this group to run this command.");
				return true;
			}

			String output = "[NLID]: [GROUPNAME] " + group.getName() + " : [MEMBERSHIPLEVEL] " + group.getPlayerType(playerUUID) + " : [PERMS] " + permissions.listPermsforPlayerType(group.getPlayerType(playerUUID));

			output += " : [OWNERS]";
			for(UUID ownerUUID : group.getAllMembers(PlayerType.OWNER))
			{
				output += " " + NameAPI.getCurrentName(ownerUUID);
			}

			output += " : [ADMINS]";
			for(UUID adminUUID : group.getAllMembers(PlayerType.ADMINS))
			{
				output += " " + NameAPI.getCurrentName(adminUUID);
			}

			output += " : [MODS]";
			for(UUID modUUID : group.getAllMembers(PlayerType.MODS))
			{
				output += " " + NameAPI.getCurrentName(modUUID);
			}

			output += " : [MEMBERS]";
			for(UUID memberUUID : group.getAllMembers(PlayerType.MEMBERS))
			{
				output += " " + NameAPI.getCurrentName(memberUUID);
			}

			if(permissions.isAccessible(pType, PermissionType.LIST_PERMS))
			{
				output += " : [OWNER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.OWNER);
				output += " : [ADMIN-PERMS] " + permissions.listPermsforPlayerType(PlayerType.ADMINS);
				output += " : [MOD-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MODS);
				output += " : [MEMBER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MEMBERS);
			}

			player.sendMessage(ChatColor.GREEN + output);
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) 
	{
		return null;
	}
}
