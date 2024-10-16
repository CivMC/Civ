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

public class GlobalChat extends BaseCommand {
	@CommandAlias("global|globalchat")
	@Syntax("[message]")
	@Description("Enters global group chat or sends a message to global group chat")
	@CommandCompletion("@CC_Groups @nothing")

	public void execute(Player player, @Optional String chatMessage) {
		String globalGroupName = CivChat2.getInstance().getPluginConfig().getGlobalChatGroupName();
		if (globalGroupName == null) {
			return;
		}
		Group globalGroup = GroupManager.getGroup(globalGroupName);
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Group currentGroup = chatMan.getGroupChatting(player);

		if (globalGroup == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return;
		}
		if (!NameAPI.getGroupManager().hasAccess(globalGroup, player.getUniqueId(),
				PermissionType.getPermission("WRITE_CHAT"))) {
			player.sendMessage(ChatStrings.chatGroupNoPerms);
			return;
		}
		if (CivChat2.getInstance().getDatabaseManager().isIgnoringGroup(player.getUniqueId(), globalGroupName)) {
			player.sendMessage(String.format(ChatStrings.chatNeedToUnignore, globalGroupName));
			return;
		}
		if (chatMessage == null) {
			if (currentGroup != null) { // Check if currently in group
				if (currentGroup == globalGroup) { // Check if current group global
					player.sendMessage(ChatStrings.chatGroupAlreadyChatting);
				} else { // Switch to global group from current group
					player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, globalGroupName));
					chatMan.removeGroupChat(player);
					chatMan.addGroupChat(player, globalGroup);
				}
			} else { // Switch to global group
				player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, globalGroupName));
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.addGroupChat(player, globalGroup);
			}
		} else { // Send message to global group
			if (currentGroup != null) {
				chatMan.sendGroupMsg(player, globalGroup, chatMessage);
			} else {
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.sendGroupMsg(player, globalGroup, chatMessage);
			}
		}
	}
}
