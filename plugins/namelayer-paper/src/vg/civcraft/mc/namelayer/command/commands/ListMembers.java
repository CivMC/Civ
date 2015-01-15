package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommand;
import vg.civcraft.mc.namelayer.group.Group;

public class ListMembers extends PlayerCommand{

	public ListMembers(String name) {
		super(name);
		setDescription("This command is used to list the members in a group");
		setUsage("/nlgroupslistmembers <group> (PlayerType)");
		setIdentifier("nlgroupslistmembers");
		setArguments(1,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage("\"Pretend this is red:\" no.");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		UUID uuid = NameAPI.getUUID(p.getName());
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return true;
		}
		
		List<UUID> uuids = null;
		if (args.length > 1){
			PlayerType type = PlayerType.getPlayerType(args[1]);
			if (type == null){
				PlayerType.displayPlayerTypes(p);
				return true;
			}
			uuids = g.getAllMembers(type);
		}
		else
			uuids = g.getAllMembers();
		String x = "Members are as follows: ";
		for (UUID uu: uuids)
			x += NameAPI.getCurrentName(uu) + " ";
		p.sendMessage(ChatColor.GREEN + x);
		return true;
	}

}
