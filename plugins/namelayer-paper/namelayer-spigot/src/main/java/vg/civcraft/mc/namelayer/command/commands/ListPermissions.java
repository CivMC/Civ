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
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.GroupPermission;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nllp")
public class ListPermissions extends BaseCommandMiddle {

	@Syntax("/nllp <group> <PlayerType>")
	@Description("Show permissions for a PlayerType in a specific group.")
	public void execute(CommandSender sender, String groupName, @Optional String playerRank) {
		if (!(sender instanceof Player)){
			sender.sendMessage(ChatColor.RED + "No.");
			return;
		}
		Player p = (Player) sender;
		Group g = gm.getGroup(groupName);
		if (groupIsNull(sender, groupName, g)) {
			return;
		}
		UUID uuid = NameAPI.getUUID(p.getName());
		PlayerType playerType = g.getPlayerType(uuid);
		if (playerType == null){
			p.sendMessage(ChatColor.RED + "You do not have access to this group.");
			return;
		}
		String perms = null;
		GroupPermission gPerm = gm.getPermissionforGroup(g);
		if(playerRank != null){
			if (!gm.hasAccess(g, uuid, PermissionType.getPermission("LIST_PERMS"))){
					p.sendMessage(ChatColor.RED + "You do not have permission in this group to run this command.");
					return;
			}
			PlayerType check = PlayerType.getPlayerType(playerRank);
			if (check == null){
				PlayerType.displayPlayerTypes(p);
				return;
			}
			perms = gPerm.listPermsforPlayerType(check);
		}
		else
			perms = gPerm.listPermsforPlayerType(playerType);
			
		p.sendMessage(ChatColor.GREEN + perms);
	}

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
