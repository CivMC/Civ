package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.namelayer.GroupManager;
import vg.civcraft.mc.namelayer.NameAPI;
import vg.civcraft.mc.namelayer.group.Group;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class GroupChat extends ChatCommand {

	public GroupChat(String name) {

		super(name);
		setIdentifier("groupc");
		setDescription("Joins a group chat");
		setUsage("/groupc <group> <message>");
		setErrorOnTooManyArgs(false);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		GroupManager gm = NameAPI.getGroupManager();
		boolean isGroupChatting = true;
		logger.debug("chatMan = [" + chatMan.toString() + "]");
		if (chatMan.getGroupChatting(player()) == null) {
			isGroupChatting = false;
		}
		Group group;
		boolean defGroup = false;
		if (args.length < 1) {
			// Check if player is in groupchat and move them to normal chat
			if (isGroupChatting) {
				msg(ChatStrings.chatMovedToGlobal);
				chatMan.removeGroupChat(player());
				return true;
			} else {
				String grpName = gm.getDefaultGroup(player().getUniqueId());
				if (grpName != null) {
					group = GroupManager.getGroup(grpName);
					defGroup = true;
				} else {
					return false;
				}
			}
		} else {
			group = argAsGroup(0);
		}
		if (group == null) {
			msg(ChatStrings.chatGroupNotFound);
			return true;
		}
		if (!NameAPI.getGroupManager().hasAccess(group, player().getUniqueId(), PermissionType.getPermission("WRITE_CHAT"))) {
			msg(ChatStrings.chatGroupNoPerms);
			return true;
		}
		if (plugin.getDatabaseManager().isIgnoringGroup(sender.getName(), group.getName())) {
			msg(ChatStrings.chatNeedToUnignore, group.getName());
			return true;
		}
		if (args.length == 1) {
			if (isGroupChatting) {
				// Player already groupchatting check if it's this group
				Group curGroup = chatMan.getGroupChatting(player());
				if (curGroup == group) {
					msg(ChatStrings.chatGroupAlreadyChatting);
					return true;
				} else {
					msg(ChatStrings.chatGroupNowChattingIn, group.getName());
					chatMan.removeGroupChat(player());
					chatMan.addGroupChat(player(), group);
					return true;
				}
			} else {
				msg(ChatStrings.chatGroupNowChattingIn, group.getName());
				if (chatMan.getChannel(player()) != null) {
					chatMan.removeChannel(player());
				}
				chatMan.addGroupChat(player(), group);
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
				Group curGroup = chatMan.getGroupChatting(player());
				if (curGroup == group) {
					chatMan.sendGroupMsg(player(), group, chatMsg.toString());
					return true;
				} else {
					chatMan.sendGroupMsg(player(), group, chatMsg.toString());
					return true;
				}
			} else {
				if (chatMan.getChannel(player()) != null) {
					chatMan.removeChannel(player());
				}
				chatMan.sendGroupMsg(player(), group, chatMsg.toString());
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {

		if (arg1.length == 0) {
			return null;
		}
		return findGroups(arg1[0]);
	}
}
