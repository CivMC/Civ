package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class LinkGroups extends PlayerCommandMiddle {

	public LinkGroups(String name) {
		super(name);
		setIdentifier("nllink");
		setDescription("Links two groups to each other as nested groups.");
		setUsage("/nllink <super group> <sub group>");
		setArguments(2,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE 
					+ "And it feels like I am just to close to "
					+ "touch you, but you are not a player.");
			return true;
		}
		Player p = (Player) sender;
			
		String supername = args[0], subname = args[1];
		
		Group supergroup = GroupManager.getGroup(supername);
		if (groupIsNull(sender, supername, supergroup)) { 
		    return true; 
		}
		
		Group subgroup = GroupManager.getGroup(subname);
		if (groupIsNull(sender, subname, subgroup)) { 
		    return true; 
		}
		
		if(subgroup.getName().equalsIgnoreCase(supergroup.getName())) {
			p.sendMessage(ChatColor.RED + "Not today");
			return true;
		}
		
		// check if groups are accessible
		
		UUID uuid = NameAPI.getUUID(p.getName());
		
		if (!supergroup.isMember(uuid) || !subgroup.isMember(uuid)) {
			p.sendMessage(ChatColor.RED + "You're not on one of the groups.");
			return true;
		}
		
		if (supergroup.isDisciplined() || subgroup.isDisciplined()) {
			p.sendMessage(ChatColor.RED + "One of the groups is disciplined.");
			return true;
		}		
		
		if (!gm.hasAccess(subgroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the sub group.");
			return true;
		}
		if (!gm.hasAccess(supergroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the super group.");
			return true;
		}
		
		if (Group.areLinked(supergroup, subgroup)) {
			p.sendMessage(ChatColor.RED + "These groups are already linked.");
			return true;
		}
		
		boolean success = Group.link(supergroup, subgroup, true);
		
		String message;
		if (success) {
			message = ChatColor.GREEN + "The groups have been successfully linked.";
		} else {
			message = ChatColor.RED + "Failed to link the groups.";
		}
		p.sendMessage(message);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.BLUE 
					+ "Fight me, bet you wont.\n "
					+ "Just back off you don't belong here.");
			return null;
		}

		if (args.length > 0) {
			return GroupTabCompleter.complete(args[args.length - 1], 
					PermissionType.getPermission("LINKING"), (Player)sender);
		} else {
			return GroupTabCompleter.complete(null, 
					PermissionType.getPermission("LINKING"), (Player)sender);
		}
	}
}
