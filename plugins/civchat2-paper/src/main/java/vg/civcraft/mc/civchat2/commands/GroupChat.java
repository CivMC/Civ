package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.List;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupChat extends BaseCommand {

	@CommandAlias("groupc|groupchat|gchat|g|gc")
	@Syntax("[group] [message]")
	@Description("Enters a group chat or sends a message to a group chat")
	public void execute(Player player, @Optional String targetGroup, @Optional String chatMessage) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		GroupManager gm = NameAPI.getGroupManager();
		boolean isGroupChatting = true;
		if (chatMan.getGroupChatting(player) == null) {
			isGroupChatting = false;
		}
		Group group;
		boolean defGroup = false;
		if (!targetGroup.isEmpty() && !chatMessage.isEmpty()) {
			// Check if player is in groupchat and move them to normal chat
			if (isGroupChatting) {
				player.sendMessage(ChatStrings.chatMovedToGlobal);
				chatMan.removeGroupChat(player);
				return;
			} else {
				String grpName = gm.getDefaultGroup(player.getUniqueId());
				if (grpName != null) {
					group = GroupManager.getGroup(grpName);
					defGroup = true;
				} else {
					return;
				}
			}
		} else {
			group = GroupManager.getGroup(targetGroup);
		}
		if (group == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
				PermissionType.getPermission("WRITE_CHAT"))) {
			player.sendMessage(ChatStrings.chatGroupNoPerms);
			return;
		}
		if (CivChat2.getInstance().getDatabaseManager().isIgnoringGroup(player.getUniqueId(), group.getName())) {
			player.sendMessage(String.format(ChatStrings.chatNeedToUnignore, group.getName()));
			return;
		}
		if (!targetGroup.isEmpty() && chatMessage == null) {
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					player.sendMessage(ChatStrings.chatGroupAlreadyChatting);
					return;
				} else {
					player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
					chatMan.removeGroupChat(player);
					chatMan.addGroupChat(player, group);
					return;
				}
			} else {
				player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.addGroupChat(player, group);
				return;
			}
		} else if (!targetGroup.isEmpty() && !chatMessage.isEmpty()) {
			StringBuilder chatMsg = new StringBuilder();
			chatMsg.append(chatMessage);
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
					return;
				} else {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
					return;
				}
			} else {
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.sendGroupMsg(player, group, chatMsg.toString());
				return;
			}
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("WRITE_CHAT"), (Player) sender);
		}
		return GroupTabCompleter.complete(args[0], PermissionType.getPermission("WRITE_CHAT"), (Player) sender);
	}
}
