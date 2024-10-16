package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;

public class ListGroups extends BaseCommandMiddle {

	@CommandAlias("nllg|listgroups|groups")
	@Syntax("[page]")
	@Description("List groups.")
	public void execute(CommandSender sender, @Optional String pageNumber) {
		Player p = null;
		UUID uuid = null;
		boolean autopages = false;
		
		if ((sender.isOp() || sender.hasPermission("namelayer.admin"))) {
			if (pageNumber == null) {
				uuid = NameAPI.getUUID(sender.getName());
			} else if (pageNumber != null) {
				uuid = NameAPI.getUUID(sender.getName());
			}
				
			if (uuid == null) {
            	sender.sendMessage(ChatColor.RED + "UUID is NULL, OP Usage is /nllg <playername>");
            	return;
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
		if (pages == 0) {
			pages = 1;
		}
		int actualPages = pages;
		
		int target = 1;
		if (pageNumber != null) {
			try {
				target = Integer.parseInt(pageNumber);
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + pageNumber + " is not a number");
				return;
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
		for (int page = target; page <= pages; page++) {
			sb.append("Page ");
			sb.append(page);
			sb.append(" of ");
			sb.append(actualPages);
			sb.append(".\n");
			
			int first = (page - 1) * 10;
			for (int x = first; x < first + 10 && x < groups.size(); x++){
				Group g = GroupManager.getGroup(groups.get(x));
				sb.append(String.format("%s : (%s)\n", 
				        g.getName(), g.getPlayerType(uuid).toString()));
			}
		}
		sender.sendMessage(sb.toString());
	}
}
