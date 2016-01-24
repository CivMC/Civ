package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class ListGroups extends PlayerCommandMiddle {

	public ListGroups(String name) {
		super(name);
		setIdentifier("nllg");
		setDescription("List groups.");
		setUsage("/nllg <page>");
		setArguments(0,1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = null;
		UUID uuid = null;
		boolean autopages = false;
		
		if ((sender.isOp() && sender.hasPermission("namelayer.admin"))) {
			if (args.length == 0) {
				uuid = NameAPI.getUUID(sender.getName());
			} else if (args.length == 1) {
				uuid = NameAPI.getUUID(args[0]);
			}
				
			if (uuid == null) {
            	sender.sendMessage(ChatColor.RED + "UUID is NULL, OP Usage is /nllg <playername>");
            	return true;
            }
            autopages = true;
        } else {
			p = (Player) sender;
			uuid = NameAPI.getUUID(p.getName());
		}
		
		List<String> groups = gm.getAllGroupNames(uuid);
		
		int pages = (groups.size() / 10);
		if (groups.size() % 10 > 0) {
			pages++;
		}
		
		int target = 0;
		try {
			target = Integer.parseInt(args[0]);
		} catch (Exception e) {
			if (e.getCause() instanceof NumberFormatException) {
				p.sendMessage(ChatColor.RED + "The page must be an integer.");
				return true;
			}
		}
		
		if (target >= pages) {
			target = pages;
		}
		
		if (!autopages) {
			pages = target;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GREEN);
		for (int page = target; page < pages; page++) {
			sb.append("Page ");
			sb.append(page + 1);
			sb.append(" of ");
			sb.append(pages);
			sb.append(".\n");
			
			int first = page * 10;
			for (int x = first; x < first + 10 && x < groups.size(); x++){
				Group g = GroupManager.getGroup(groups.get(x));
				sb.append(String.format("%s : (%s)\n", 
				        g.getName(), g.getPlayerType(uuid).toString()));
			}
		}
		sender.sendMessage(sb.toString());
		return true;
	}
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
