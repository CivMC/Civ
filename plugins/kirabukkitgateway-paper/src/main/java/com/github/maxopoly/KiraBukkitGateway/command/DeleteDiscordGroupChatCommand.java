package com.github.maxopoly.KiraBukkitGateway.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.github.maxopoly.KiraBukkitGateway.KiraBukkitGatewayPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class DeleteDiscordGroupChatCommand extends BaseCommand {

	@CommandAlias("deletediscordchannel")
	@Description("Delete the Discord channel linked to a group")
	@Syntax("<group>")
	public void execute(Player player, String groupName) {
		Group group = GroupManager.getGroup(groupName);
		if (group == null) {
			player.sendMessage(ChatColor.RED + "That group does not exist");
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
				PermissionType.getPermission("KIRA_MANAGE_CHANNEL"))) {
			player.sendMessage(ChatColor.RED + "You do not have permission to do that");
			return;
		}
		KiraBukkitGatewayPlugin.getInstance().getRabbit().deleteGroupChatChannel(group.getName(),player.getUniqueId());
		player.sendMessage(ChatColor.GREEN + "Attempting to delete channel...");
	}
}
