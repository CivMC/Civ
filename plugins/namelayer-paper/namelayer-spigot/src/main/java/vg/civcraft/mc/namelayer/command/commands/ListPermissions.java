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
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ListPermissions extends PlayerCommandMiddle{

	public ListPermissions(String name) {
		super(name);
		setIdentifier("nllp");
		setDescription("Show permissions for a PlayerType in a specific group.");
		setUsage("/nllp <group> <PlayerType>");
		setArguments(1,2);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "No.");
			return true;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(args[0]);
		if (groupIsNull(sender, args[0], g)) {
			return true;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType playerType = g.getPlayerType(uuid);
		if (playerType == null){
			p.sendMessage(ChatColor.RED + "You do not have access to this group.");
			return true;
		}
		String perms = null;
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if(args.length > 1){
			if (!gm.hasAccess(g, uuid, PermissionType.getPermission("LIST_PERMS"))){
					p.sendMessage(ChatColor.RED + "You do not have permission in this group to run this command.");
					return true;
			}
			PlayerType check = PlayerType.getPlayerType(args[1]);
			if (check == null){
				PlayerType.displayPlayerTypes(p);
				return true;
			}
			perms = gPerm.listPermsforPlayerType(check);
		}
		else
			perms = gPerm.listPermsforPlayerType(playerType);
			
		p.sendMessage(ChatColor.GREEN + perms);
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

		return  null;
	}

}
