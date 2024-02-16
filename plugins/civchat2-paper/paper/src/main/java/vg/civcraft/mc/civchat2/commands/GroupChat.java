package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupChat extends BaseCommand {

	@CommandAlias("groupc|groupchat|gchat|g|gc")
	@Syntax("[group] [message]")
	@Description("Enters a group chat or sends a message to a group chat")
	@CommandCompletion("@CC_Groups @nothing")
	public void execute(Player player, @Optional String targetGroup, @Optional String chatMessage) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		GroupManager gm = NameAPI.getGroupManager();
		boolean isGroupChatting = chatMan.getGroupChatting(player) != null;
		Group group;
		boolean defGroup = false;
		if (targetGroup == null && chatMessage == null) {
			// Check if player is in groupchat and move them to normal chat
			if (isGroupChatting) {
				player.sendMessage(ChatStrings.chatMovedToGlobal);
				chatMan.removeGroupChat(player);
				return;
			} else {
				String defGroupName = gm.getDefaultGroup(player.getUniqueId());
				if (defGroupName != null) {
					group = GroupManager.getGroup(defGroupName);
					defGroup = true;
				} else {
					return;
				}
			}
		}
		if (targetGroup == null) {
			return;
		}
		group = GroupManager.getGroup(targetGroup);
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
		if (chatMessage == null) {
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					player.sendMessage(ChatStrings.chatGroupAlreadyChatting);
				} else {
					player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
					chatMan.removeGroupChat(player);
					chatMan.addGroupChat(player, group);
				}
			} else {
				player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.addGroupChat(player, group);
			}
		} else {
			StringBuilder chatMsg = new StringBuilder();
			chatMsg.append(chatMessage);
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
				} else {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
				}
			} else {
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.sendGroupMsg(player, group, chatMsg.toString());
			}
		}
	}
}
