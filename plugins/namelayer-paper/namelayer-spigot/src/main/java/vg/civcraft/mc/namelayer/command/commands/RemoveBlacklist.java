package vg.civcraft.mc.namelayer.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.BlackList;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class RemoveBlacklist extends PlayerCommandMiddle {
	
	public RemoveBlacklist(String name) {
		super(name);
		setIdentifier("nlubl");
		setDescription("Removes a player from the blacklist for a specific group");
		setUsage("/nlubl <group> <player>");
		setArguments(2, 2);
	}

	@Override
	public boolean execute(CommandSender arg0, String[] arg1) {
		if (!(arg0 instanceof Player)) {
			arg0.sendMessage(ChatColor.RED
					+ "Why do you have to make this so difficult?");
			return true;
		}
		Player p = (Player) arg0;
		Group g = GroupManager.getGroup(arg1[0]);
		if (g == null) {
			p.sendMessage(ChatColor.RED + "This group does not exist");
			return true;
		}
		if (!gm.hasAccess(g, p.getUniqueId(),
				PermissionType.getPermission("BLACKLIST"))
				&& !(p.isOp() || p.hasPermission("namelayer.admin"))) {
			p.sendMessage(ChatColor.RED + "You do not have the required permissions to do this");
			return true;
		}
		UUID targetUUID = NameAPI.getUUID(arg1[1]);
		if (targetUUID == null) {
			p.sendMessage(ChatColor.RED + "This player does not exist");
			return true;
		}
		BlackList bl = NameLayerPlugin.getBlackList();
		if (!bl.isBlacklisted(g, targetUUID)) {
			p.sendMessage(ChatColor.RED + "This player is not blacklisted");
			return true;
		}
		bl.removeBlacklistMember(g, targetUUID, true);
		p.sendMessage(ChatColor.GREEN + NameAPI.getCurrentName(targetUUID) + " was successfully removed from the blacklist for the group " + g.getName());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "I'm sorry baby, please run this as a player :)");
			return null;
		}
		if (args.length < 2) {
			if (args.length == 0)
				return GroupTabCompleter.complete(null, null, (Player) sender);
			else
				return GroupTabCompleter.complete(args[0], null, (Player)sender);

		} else if (args.length == 2) {
			List<String> namesToReturn = new ArrayList<String>();
			for (Player p: Bukkit.getOnlinePlayers()) {
				if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
					namesToReturn.add(p.getName());
			}
			return namesToReturn;
		}
		return null;
	}
}
