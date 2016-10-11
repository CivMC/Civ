package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;
import vg.civcraft.mc.namelayer.group.Group;

public class IgnoreGroup extends ChatCommand {

	public IgnoreGroup(String name) {
		super(name);
		setIdentifier("ignoregroup");
		setDescription("Toggles ignoring a group");
		setUsage("/ignoregroup <group>");
		setArguments(1,1);
		setSenderMustBePlayer(true);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		Group group = argAsGroup(0);
		if(group == null) {
			//no player exists with that name
			msg(ChatStrings.chatGroupNotFound);
			return true;
		}
		String ignore = group.getName();
		String name = getRealName(player());
		if(!DBM.isIgnoringGroup(name, ignore)) {
			//Player added to list
			DBM.addIgnoredGroup(name, ignore);
			String debugMessage = "Player ignored Group, Player: " + name + " Group: " + ignore;
			logger.debug(debugMessage);
			msg(ChatStrings.chatNowIgnoring, ignore);
			if (group.equals(chatMan.getGroupChatting(player()))) {
				chatMan.removeGroupChat(player());
				msg(ChatStrings.chatMovedToGlobal);
			}
			return true;
		} else {
			//player removed from list
			DBM.removeIgnoredGroup(name, ignore);
			String debugMessage = "Player un-ignored Group, Player: " + name + " Group: " + ignore;
			logger.debug(debugMessage);
			msg(ChatStrings.chatStoppedIgnoring, ignore);
			return true;
		}		
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		if (arg1.length != 1) {
			return null;
		}

		return findGroups(arg1[0]);	
	}	

}
