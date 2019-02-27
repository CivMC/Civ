package com.github.maxopoly.KiraBukkitGateway.command.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;

import vg.civcraft.mc.civmodcore.command.PlayerCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class CreateDiscordGroupChatCommand extends PlayerCommand {

	public CreateDiscordGroupChatCommand() {
		super("linkdiscordchannel");
		setIdentifier("linkdiscordchannel");
		setDescription("Create a discord channel to which group chats and snitch alerts will be forwarded");
		setUsage("/linkdiscordchannel [group]");
		setArguments(1, 1);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You are not a player");
			return true;
		}
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
		GroupManager gm = NameAPI.getGroupManager();
		PermissionType perm = PermissionType.getPermission("READ_CHAT");
		Collection<UUID> members = new HashSet<>();
		group.getAllMembers().stream().filter(m -> gm.hasAccess(group, m, perm)).forEach(m -> members.add(m));
		KiraBukkitGatewayPlugin.getInstance().getRabbit().createGroupChatChannel(group.getName(), members,
				p.getUniqueId(), -1L, -1L);
		sender.sendMessage(ChatColor.GREEN + "Attempting to create channel...");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			return null;
		}
		if (args.length == 0) {
			return GroupTabCompleter.complete("", null, (Player) sender);
		}
		return GroupTabCompleter.complete(args[0], null, (Player) sender);
	}

}
