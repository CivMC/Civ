package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class InfoDump extends PlayerCommandMiddle
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
			GroupPermission permissions = gm.getPermissionforGroup(group);
			StringBuilder outputBuilder = new StringBuilder();
			outputBuilder.append("[NLID] : [GROUPNAME] ");
			outputBuilder.append(group.getName());
			outputBuilder.append(" : [MEMBERSHIPLEVEL] ");
			outputBuilder.append(group.getPlayerType(playerUUID));
			outputBuilder.append(" : [PERMS] ");
			outputBuilder.append(permissions.listPermsforPlayerType(group.getPlayerType(playerUUID)));
			

			outputBuilder.append(" : [OWNERS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("OWNER")))
			{
				for(UUID ownerUUID : group.getAllMembers(PlayerType.OWNER))
				{
					outputBuilder.append(" " + NameAPI.getCurrentName(ownerUUID));
				}
			}
			else
			{
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.OWNER).size());
			}

			outputBuilder.append(" : [ADMINS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("ADMINS")))
			{
				for(UUID adminUUID : group.getAllMembers(PlayerType.ADMINS))
				{
					outputBuilder.append(" " + NameAPI.getCurrentName(adminUUID));
				}
			}
			else
			{
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.ADMINS).size());
			}

			outputBuilder.append(" : [MODS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("MODS")))
			{
				for(UUID modUUID : group.getAllMembers(PlayerType.MODS))
				{
					outputBuilder.append(" " + NameAPI.getCurrentName(modUUID));
				}
			}
			else
			{
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.MODS).size());
			}

			outputBuilder.append(" : [MEMBERS]");
			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("MEMBERS")))
			{
				for(UUID memberUUID : group.getAllMembers(PlayerType.MEMBERS))
				{
					outputBuilder.append(" " + NameAPI.getCurrentName(memberUUID));
				}
			}
			else
			{
				outputBuilder.append(" accounts-");
				outputBuilder.append(group.getAllMembers(PlayerType.MEMBERS).size());
			}

			if(gm.hasAccess(group, playerUUID, PermissionType.getPermission("LIST_PERMS")))
			{
				outputBuilder.append(" : [OWNER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.OWNER));
				outputBuilder.append(" : [ADMIN-PERMS] " + permissions.listPermsforPlayerType(PlayerType.ADMINS));
				outputBuilder.append(" : [MOD-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MODS));
				outputBuilder.append(" : [MEMBER-PERMS] " + permissions.listPermsforPlayerType(PlayerType.MEMBERS));
			}

			player.sendMessage(ChatColor.GREEN + outputBuilder.toString());
			return true;
		}
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) 
	{
		return null;
	}
}
