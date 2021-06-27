package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.GroupManager.PlayerType;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.command.TabCompleters.MemberTypeCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nllm")
public class ListMembers extends BaseCommandMiddle {

	@Syntax("/nllm <group> (PlayerType)")
	@Description("List the members in a group")
	public void execute(CommandSender sender, String groupName, @Optional String playerType, @Optional String playerName) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "No can do.");
			return;
		}
		
		Player p = (Player) sender;
		UUID uuid = NameAPI.getUUID(p.getName());
		String groupname = groupName;
		
		Group group = GroupManager.getGroup(groupname);
		if (groupIsNull(sender, groupname, group)) {
			return;
		}
		
		if (!p.hasPermission("namelayer.admin")) {
			if (!group.isMember(uuid)) {
				p.sendMessage(ChatColor.RED + "You're not on this group.");
				return;
			}
	
			if (!gm.hasAccess(group, uuid, PermissionType.getPermission("GROUPSTATS"))) {
				p.sendMessage(ChatColor.RED 
						+ "You don't have permission to run that command.");
				return;
			}
		}
		
		List<UUID> uuids = null;
		if (playerType != null && playerName != null) {
			String nameMin = playerType, nameMax = playerName;
			
			List<UUID> members = group.getAllMembers();
			uuids = Lists.newArrayList();
			
			for (UUID member : members) {
				String name = NameAPI.getCurrentName(member);
				if (name.compareToIgnoreCase(nameMin) >=0 
						&& name.compareToIgnoreCase(nameMax) <= 0) {
					uuids.add(member);
				}
			}
		} else if (playerType != null && playerName == null) {
			String playerRank = playerType;
			PlayerType filterType = PlayerType.getPlayerType(playerRank);
			
			if (filterType == null) {
				// user entered invalid type, show them
				PlayerType.displayPlayerTypes(p);
				return;
			}
			
			uuids = group.getAllMembers(filterType);
		} else {
			uuids = group.getAllMembers();
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GREEN);
		sb.append("Members are as follows:\n");
		for (UUID uu: uuids){
			sb.append(NameAPI.getCurrentName(uu));
			sb.append(" (");
			sb.append(group.getPlayerType(uu));
			sb.append(")\n");
		}
		
		p.sendMessage(sb.toString());
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
			
		if (args.length == 0)
			return GroupTabCompleter.complete(null, null, (Player) sender);
		else if (args.length == 1)
			return GroupTabCompleter.complete(args[0], null, (Player)sender);
		else if (args.length == 2)
			return MemberTypeCompleter.complete(args[1]);

		return null;
	}
}
