package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
		Boolean autopages = false;
		if ((sender.isOp() && sender.hasPermission("namelayer.admin"))) {
			if (args.length == 0)
				uuid = NameAPI.getUUID(sender.getName());
			else if (args.length == 1)
				uuid = NameAPI.getUUID(args[0]);
            if(uuid == null){
            	sender.sendMessage(ChatColor.RED + "UUID is NULL,  OP Usage is /nllg <playername>");
            	return true;
            }
            autopages = true;
        }
		else{
			p = (Player) sender;
			uuid = NameAPI.getUUID(p.getName());
		}
		
		List<String> groups = gm.getAllGroupNames(uuid);
		
		int pages = (groups.size() / 10) + 1;
		String names = ChatColor.GREEN + "";
		int start = 1;
		try {
			start = Integer.parseInt(args[0]);
		} catch(NumberFormatException | ArrayIndexOutOfBoundsException e){
			if (e.getCause() instanceof NumberFormatException){
				p.sendMessage(ChatColor.RED + "The page must be an integer.");
				return true;
			}
		}
		
		
		if(autopages){
			for(int curPage = 1; curPage <= pages; curPage++)
				{
					start = curPage;
					names += "Page " + start + " of " + pages + ".\n"
							+ "Groups are as follows: \n";
					for (int x = (start-1) * 10, z = 1; x < groups.size() && z <= 10; x++, z++){
						Group g = gm.getGroup(groups.get(x));
						names += g.getName() + ": (PlayerType) " + g.getPlayerType(uuid).toString() + "\n";
					}
				}
			sender.sendMessage(names);
		}
		else{
			names += "Page " + start + " of " + pages + ".\n"
					+ "Groups are as follows: \n";
			for (int x = (start-1) * 10, z = 1; x < groups.size() && z <= 10; x++, z++){
				Group g = gm.getGroup(groups.get(x));
				names += g.getName() + ": (PlayerType) " + g.getPlayerType(uuid).toString() + "\n";
			}
			p.sendMessage(names);
		}
		return true;
	}
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
