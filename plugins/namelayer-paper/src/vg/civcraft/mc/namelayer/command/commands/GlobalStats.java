package vg.civcraft.mc.namelayer.command.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.List;
import java.util.UUID;

public class GlobalStats extends PlayerCommand{

	public GlobalStats(String name) {
		super(name);
		setIdentifier("nlgls");
		setDescription("This command is used to get stats about groups and the sorts.");
		setUsage("/nlgls");
		setArguments(0,0);
		
		
	}

	/*@Override
	public boolean execute(final CommandSender sender, String[] args) {
		Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), new Runnable(){

			@Override
			public void run() {
				int count = NameLayerPlugin.getGroupManagerDao().countGroups();
				sender.sendMessage(ChatColor.GREEN + "The amount of groups are: " + count);
			}
			
		});
		sender.sendMessage(ChatColor.GREEN + "Stats are being retrieved, please wait.");
		return true;
	}*/
	
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
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
