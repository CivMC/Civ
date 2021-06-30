package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ListSubGroups extends BaseCommandMiddle {

	private static String format = "%s%s : (%s)\n";

	@CommandAlias("nllsg|subgroups|listsubgroups")
	@Syntax("[group]")
	@Description("List the nested group hierarchy.")
	public void execute(CommandSender sender, @Optional String targetGroupName) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "No can do.");
			return;
		}
		
		UUID uuid = NameAPI.getUUID(sender.getName());
		
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.GREEN);
		sb.append("Group Hierarchy is as below:\n");
		
		if (targetGroupName != null) {
			String groupname = targetGroupName;
			
			Group group = GroupManager.getGroup(groupname);
			if (groupIsNull(sender, groupname, group)) {
				return;
			}
			
			if (!gm.hasAccess(group, uuid, PermissionType.getPermission("GROUPSTATS"))) {
				sender.sendMessage(ChatColor.RED 
						+ "You don't have permission to run that command.");
				return;
			}
			
			sb.append(String.format(format, "", group.getName(), group.getPlayerType(uuid)));
			buildList(sb, uuid, group.getSubgroups(), "   ");
		} else {
			List<String> groups = gm.getAllGroupNames(uuid);
			Set<String> supergroups = Sets.newHashSet(groups);
			
			for (String groupname : groups) {
				Group group = GroupManager.getGroup(groupname);
				removeSubs(group.getSubgroups(), supergroups);
			}
			
			for (String supergroup : supergroups) {
				Group group = GroupManager.getGroup(supergroup);
				sb.append(String.format(format, "", group.getName(), group.getPlayerType(uuid)));
				buildList(sb, uuid, group.getSubgroups(), "   ");
			}
		}
		sender.sendMessage(sb.toString());
	}

	private void removeSubs(List<Group> subs, Set<String> groups) {
		for (Group group : subs) {
			groups.remove(group.getName());
			removeSubs(group.getSubgroups(), groups);
		}
	}

	private void buildList(StringBuilder sb, UUID uuid, List<Group> subs, String prefix) {
		for (Group group : subs) {
			sb.append(String.format(format, prefix, group.getName(), group.getPlayerType(uuid)));
			buildList(sb, uuid, group.getSubgroups(), "   " + prefix);
		}
	}
}
