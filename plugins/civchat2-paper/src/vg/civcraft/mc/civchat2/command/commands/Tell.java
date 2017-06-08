package vg.civcraft.mc.civchat2.command.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Tell extends ChatCommand {

	public Tell(String name) {
		super(name);
		setIdentifier("tell");
		setDescription("Sends a private message to another player");
		setUsage("/tell <player> <message>");
		setSenderMustBePlayer(true);
		setErrorOnTooManyArgs(false);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		if (args.length == 0) {
			UUID chattingWith = chatMan.getChannel(player());
			if (chattingWith != null) {
				chatMan.removeChannel(player());
				msg(ChatStrings.chatRemovedFromChat);
			} else {
				msg(ChatStrings.chatNotInPrivateChat);
			}
			return true;
		}

		Player receiver = argAsPlayer(0);
		if (receiver == null) {
			msg(ChatStrings.chatPlayerNotFound);
			return true;
		}

		if (! (receiver.isOnline())) {
			msg(ChatStrings.chatPlayerIsOffline);
			logger.debug(parse(ChatStrings.chatPlayerIsOffline));
			return true;
		}

		if (player().equals(receiver)) {
			msg(ChatStrings.chatCantMessageSelf);
			return true;
		}

		if (args.length >= 2) {
			// Player and message
			StringBuilder builder = new StringBuilder();
			for (int x = 1; x < args.length; x++) {
				builder.append(args[x] + " ");
			}

			chatMan.sendPrivateMsg(player(), receiver, builder.toString());
			return true;
		} else if (args.length == 1) {
			if (DBM.isIgnoringPlayer(player().getUniqueId(), receiver.getUniqueId())) {
				msg(ChatStrings.chatNeedToUnignore, getRealName(receiver));
				return true;
			}

			if (DBM.isIgnoringPlayer(receiver.getUniqueId(), player().getUniqueId())) {
				msg(ChatStrings.chatPlayerIgnoringYou);
				return true;
			}
			chatMan.addChatChannel(player(), receiver);
			msg(ChatStrings.chatNowChattingWith, getRealName(receiver));
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {

		if (args.length != 1) {
			return null;
		}
		return findPlayers(args[0]);
	}
}
