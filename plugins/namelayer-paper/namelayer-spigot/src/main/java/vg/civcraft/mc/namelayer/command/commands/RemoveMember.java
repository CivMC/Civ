package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupMemberTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RemoveMember extends PlayerCommandMiddle {

	public RemoveMember(String name) {
		super(name);
		setIdentifier("nlrm");
		setDescription("Remove a member from a group.");
		setUsage("/nlrm <group> <member>");
		setArguments(2,2);
		
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "I'm sorry baby, please run this as a player :)");
			return true;
		}
		Player p = (Player) sender;
		Group group = gm.getGroup(args[0]);
		if (groupIsNull(sender, args[0], group)) {
			return true;
		}
		if (group.isDisciplined()){
			p.sendMessage(ChatColor.RED + "This group is disiplined.");
			return true;
		}
		UUID executor = NameAPI.getUUID(p.getName());
		UUID uuid = NameAPI.getUUID(args[1]);
		
		if (uuid == null){
			p.sendMessage(ChatColor.RED + "The player has never played before.");
			return true;
		}
		
		String playerName = NameAPI.getCurrentName(uuid);
		PlayerType toBeRemoved = group.getPlayerType(uuid);
		if (toBeRemoved == null){
			//hides who is actually on the group
			toBeRemoved = PlayerType.MEMBERS;
		}
		boolean allowed = false;
		switch (toBeRemoved){ // depending on the type the executor wants to add the player to
		case MEMBERS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MEMBERS"));
			break;
		case MODS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("MODS"));
			break;
		case ADMINS:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("ADMINS"));
			break;
		case OWNER:
			allowed = gm.hasAccess(group, executor, PermissionType.getPermission("OWNER"));
			break;
		}
		
		if (!allowed && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You do not have permissions to modify this group.");
			return true;
		}
		
		if (!group.isMember(uuid)){
			p.sendMessage(ChatColor.RED + "That player is not on the group.");
			return true;
		}
		
		if (group.isOwner(uuid)){
			p.sendMessage(ChatColor.RED + "That player owns the group, you cannot "
					+ "remove the player.");
			return true;
		}
		
		p.sendMessage(ChatColor.GREEN + playerName + " has been removed from the group.");
		group.removeMember(uuid);
		return true;
	}


	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length < 2) {
			if (args.length == 1)
				return GroupTabCompleter.complete(args[0], null, (Player) sender);
			else {
				return GroupTabCompleter.complete(null, null, (Player)sender);
			}
		}
		if (args.length == 2) {
			return null;
			//return GroupMemberTabCompleter.complete(args[0],args[1], (Player) sender);
		}

		return null;
	}

}