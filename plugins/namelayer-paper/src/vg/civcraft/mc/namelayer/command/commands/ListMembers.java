package vg.civcraft.mc.namelayer.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.PlayerCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ListMembers extends PlayerCommandMiddle{

	public ListMembers(String name) {
		super(name);
		setIdentifier("nllm");
		setDescription("List the members in a group");
		setUsage("/nllm <group> (PlayerType)");
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
		if (g == null){
			p.sendMessage(ChatColor.RED + "That group does not exist.");
			return true;
		}
		if (!g.isMember(uuid) && !(p.isOp() || p.hasPermission("namelayer.admin"))){
			p.sendMessage(ChatColor.RED + "You are not on this group.");
			return true;
		}
		
		if (!gm.getPermissionforGroup(g).isAccessible(g.getPlayerType(uuid), PermissionType.GROUPSTATS)){
			p.sendMessage(ChatColor.RED + "You do not have permission to run that command.");
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
		StringBuilder sb = new StringBuilder();
		sb.append("Members are as follows: ");
		for (UUID uu: uuids){
			sb.append(NameAPI.getCurrentName(uu));
			sb.append(" ");
		}
		
		p.sendMessage(ChatColor.GREEN + sb.toString());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length == 0)
			return GroupTabCompleter.complete(null, null, (Player) sender);
		else if (args.length == 1)
			return GroupTabCompleter.complete(args[0], null, (Player)sender);
		else if (args.length == 2)
			return MemberTypeCompleter.complete(args[1]);

		return null;
	}
}
