package vg.civcraft.mc.civchat2.command.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.command.ChatCommand;

public class Reply extends ChatCommand {

	public Reply(String name) {

		super(name);
		setIdentifier("reply");
		setDescription("Replies to a private message");
		setUsage("/reply <message>");
		setSenderMustBePlayer(true);
		setErrorOnTooManyArgs(false);
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {

		Player player = (Player) sender;
		String senderName = player.getName();
		UUID receiverUUID = chatMan.getPlayerReply(player);

		Player receiver = Bukkit.getPlayer(receiverUUID);
		if (receiver == null) {
			msg(ChatStrings.chatNoOneToReplyTo);
			return true;
		}

		if (!(receiver.isOnline())) {
			msg(ChatStrings.chatPlayerIsOffline);
			logger.debug(parse(ChatStrings.chatPlayerIsOffline));
			return true;
		}

		if (player.getName().equals(receiver.getName())) {
			CivChat2.warningMessage("Reply Command, Player Replying to themself??? Player: [" + senderName + "]");
			msg(ChatStrings.chatCantMessageSelf);
			return true;
		}

		if (args.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : args) {
				sb.append(s + " ");
			}
			chatMan.sendPrivateMsg(player, receiver, sb.toString());
			return true;
		} else if (args.length == 0) {
			// Player to chat with reply user
			chatMan.removeChannel(player());
			chatMan.addChatChannel(player(), receiver);
			msg(ChatStrings.chatNowChattingWith, receiver.getName());
			return true;
		}

		return false;
	}
}
