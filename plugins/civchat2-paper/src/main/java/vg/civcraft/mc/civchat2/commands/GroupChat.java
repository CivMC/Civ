package vg.civcraft.mc.civchat2.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.command.TabCompleters.GroupTabCompleter;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

@CivCommand(id = "groupc")
public class GroupChat extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Player player = (Player) sender;
		GroupManager gm = NameAPI.getGroupManager();
		boolean isGroupChatting = true;
		if (chatMan.getGroupChatting(player) == null) {
			isGroupChatting = false;
		}
		Group group;
		boolean defGroup = false;
		if (args.length < 1) {
			// Check if player is in groupchat and move them to normal chat
			if (isGroupChatting) {
				player.sendMessage(ChatStrings.chatMovedToGlobal);
				chatMan.removeGroupChat(player);
				return true;
			} else {
				String grpName = gm.getDefaultGroup(player.getUniqueId());
				if (grpName != null) {
					group = GroupManager.getGroup(grpName);
					defGroup = true;
				} else {
					return false;
				}
			}
		} else {
			group = GroupManager.getGroup(args[0]);
		}
		if (group == null) {
			player.sendMessage(ChatStrings.chatGroupNotFound);
			return true;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, player.getUniqueId(),
				PermissionType.getPermission("WRITE_CHAT"))) {
			player.sendMessage(ChatStrings.chatGroupNoPerms);
			return true;
		}
		if (CivChat2.getInstance().getDatabaseManager().isIgnoringGroup(player.getUniqueId(), group.getName())) {
			player.sendMessage(String.format(ChatStrings.chatNeedToUnignore, group.getName()));
			return true;
		}
		if (args.length == 1) {
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					player.sendMessage(ChatStrings.chatGroupAlreadyChatting);
					return true;
				} else {
					player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
					chatMan.removeGroupChat(player);
					chatMan.addGroupChat(player, group);
					return true;
				}
			} else {
				player.sendMessage(String.format(ChatStrings.chatGroupNowChattingIn, group.getName()));
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.addGroupChat(player, group);
				return true;
			}
		} else if (args.length > 1) {
			StringBuilder chatMsg = new StringBuilder();
			for (int i = defGroup ? 0 : 1; i < args.length; i++) {
				chatMsg.append(args[i]);
				chatMsg.append(" ");
			}
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player);
				if (curGroup == group) {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
					return true;
				} else {
					chatMan.sendGroupMsg(player, group, chatMsg.toString());
					return true;
				}
			} else {
				if (chatMan.getChannel(player) != null) {
					chatMan.removeChannel(player);
				}
				chatMan.sendGroupMsg(player, group, chatMsg.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 0) {
			return GroupTabCompleter.complete(null, PermissionType.getPermission("WRITE_CHAT"), (Player) sender);
		}
		return GroupTabCompleter.complete(args[0], PermissionType.getPermission("WRITE_CHAT"), (Player) sender);
	}
}
