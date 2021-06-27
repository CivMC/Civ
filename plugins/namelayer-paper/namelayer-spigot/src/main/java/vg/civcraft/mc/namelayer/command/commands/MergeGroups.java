package vg.civcraft.mc.namelayer.command.commands;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;
import vg.civcraft.mc.namelayer.command.BaseCommandMiddle;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CommandAlias("nlmg")
public class MergeGroups extends BaseCommandMiddle {

	@Syntax("/nlmg <The group left> <The group that will be gone>")
	@Description("Merge two groups together.")
	public void execute(CommandSender sender, String groupToKeep, String groupToDelete) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.BLUE + "Fight me, bet you wont.\n Just back off you don't belong here.");
			return;
		}
		final Player p = (Player) sender;
		final Group g = GroupManager.getGroup(groupToKeep);
		if (groupIsNull(sender, groupToKeep, g)) {
			return;
		}

		final Group toMerge = GroupManager.getGroup(groupToDelete);
		if (groupIsNull(sender, groupToDelete, toMerge)) {
			return;
		}

		if (g.isDisciplined() || toMerge.isDisciplined()) {
			p.sendMessage(ChatColor.RED + "One of the groups is disiplined.");
			return;
		}

		if (g == toMerge) {
			p.sendMessage(ChatColor.RED + "You cannot merge a group into itself");
			return;
		}

		UUID uuid = NameAPI.getUUID(p.getName());
		if (!gm.hasAccess(g, uuid, PermissionType.getPermission("MERGE"))) {
			p.sendMessage(ChatColor.RED + "You don't have permission on group " + g.getName() + ".");
			return;
		}
		if (!gm.hasAccess(toMerge, uuid, PermissionType.getPermission("MERGE"))) {
			p.sendMessage(ChatColor.RED + "You don't have permission on group " + toMerge.getName() + ".");
			return;
		}
		try {
			gm.mergeGroup(g, toMerge);
			p.sendMessage(ChatColor.GREEN + "Group merging is completed.");
		} catch (Exception e) {
			NameLayerPlugin.getInstance().getLogger().log(Level.SEVERE, "Group merging failed", e);
			p.sendMessage(ChatColor.GREEN + "Group merging may have failed.");
		}
		p.sendMessage(ChatColor.GREEN + "Group is under going merge.");
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player))
			return null;

		if (args.length > 0)
			return GroupTabCompleter.complete(args[args.length - 1], PermissionType.getPermission("MERGE"),
					(Player) sender);
		else {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("MERGE"), (Player) sender);
		}
	}
}
