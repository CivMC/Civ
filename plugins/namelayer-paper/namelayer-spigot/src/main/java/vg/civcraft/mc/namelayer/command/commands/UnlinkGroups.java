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

public class UnlinkGroups extends PlayerCommandMiddle {

	public UnlinkGroups(String name) {
		super(name);
		setIdentifier("nlunlink");
		setDescription("Unlinks two groups from each other.");
		setUsage("/nlunlink <super group> <sub group>");
		setArguments(2,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Sorry bruh, no can do.");
			return true;
		}
		Player p = (Player) sender;
		
		// check if groups exist
		
		String supername = args[0], subname = args[1];
		
		Group supergroup = GroupManager.getGroup(supername);
		if (groupIsNull(sender, supername, supergroup)) {
		    return true;
		}
		
		Group subgroup = GroupManager.getGroup(subname);
		if (groupIsNull(sender, subname, subgroup)) {
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
		
		if (!gm.hasAccess(supergroup, uuid, PermissionType.getPermission("LINKING"))) {
			p.sendMessage(ChatColor.RED 
					+ "You don't have permission to do that on the super group.");
			return true;
		}
		
		if (!Group.areLinked(supergroup, subgroup)) {
			p.sendMessage(ChatColor.RED + "These groups are not linked.");
			return true;
		}
		
		boolean success = Group.unlink(supergroup, subgroup);
		
		String message;
		if (success) {
			message = ChatColor.GREEN + "The groups have been successfully unlinked.";
		} else {
			message = ChatColor.RED + "Failed to unlink the groups.";
		}
		p.sendMessage(message);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		
		if (args.length > 0) {
			return GroupTabCompleter.complete(args[0], 
					PermissionType.getPermission("LINKING"), (Player)sender);
		} else {
			return GroupTabCompleter.complete(null, 
					PermissionType.getPermission("LINKING"), (Player)sender);
		}
	}
}
