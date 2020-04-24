package com.github.maxopoly.KiraBukkitGateway.command;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CivCommand(id = "deletediscordchannel")
public class DeleteDiscordGroupChatCommand extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player p = (Player) sender;
		Group group = GroupManager.getGroup(args[0]);
		if (group == null) {
			sender.sendMessage(ChatColor.RED + "That group does not exist");
			return true;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, p.getUniqueId(),
				PermissionType.getPermission("KIRA_MANAGE_CHANNEL"))) {
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that");
			return true;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().deleteGroupChatChannel(group.getName(),p.getUniqueId());
		sender.sendMessage(ChatColor.GREEN + "Attempting to delete channel...");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return GroupTabCompleter.complete("", null, (Player) sender);
		}
		return GroupTabCompleter.complete(args [0], null, (Player) sender);
	}

}
