package vg.civcraft.mc.namelayer.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ShowBlacklist extends PlayerCommandMiddle {

	public ShowBlacklist(String name) {
		super(name);
		setIdentifier("nlsbl");
		setDescription("Shows all blacklisted players for a specific group");
		setUsage("/nlsbl <group>");
		setArguments(1, 1);
	}

	@Override
	public boolean execute(CommandSender arg0, String[] arg1) {
		if (!(arg0 instanceof Player)) {
			arg0.sendMessage(ChatColor.RED
					+ "Why do you have to make this so difficult?");
			return true;
		}
		Player p = (Player) arg0;
		Group g = gm.getGroup(arg1[0]);
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
		Set <UUID> ids = NameLayerPlugin.getBlackList().getBlacklist(g);
		if (ids.size() == 0) {
			p.sendMessage(ChatColor.GOLD + "There are no blacklisted players for the group " + g.getName());
			return true;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GOLD + "Blacklisted players for group " + g.getName() + " are: ");
		for(UUID id : ids) {
			sb.append(NameAPI.getCurrentName(id));
			sb.append(", ");
		}
		String reply = sb.toString();
		//remove last ", "
		p.sendMessage(reply.substring(0, reply.length() - 2));
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
