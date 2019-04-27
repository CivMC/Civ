package vg.civcraft.mc.civchat2.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "reply")
public class Reply extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		Player player = (Player) sender;
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		UUID receiverUUID = chatMan.getPlayerReply(player);

		Player receiver = Bukkit.getPlayer(receiverUUID);
		if (receiver == null) {
			player.sendMessage(ChatStrings.chatNoOneToReplyTo);
			return true;
		}

		if (!(receiver.isOnline())) {
			player.sendMessage(ChatStrings.chatPlayerIsOffline);
			return true;
		}

		if (player.getUniqueId().equals(receiver.getUniqueId())) {
			player.sendMessage(ChatStrings.chatCantMessageSelf);
			return true;
		}

		if (args.length > 0) {
			StringBuilder sb = new StringBuilder();
			for (String s : args) {
				sb.append(s + " ");
			}
			chatMan.sendPrivateMsg(player, receiver, sb.toString());
			return true;
		}
		// Player to chat with reply user
		chatMan.removeChannel(player);
		chatMan.addChatChannel(player, receiver);
		player.sendMessage(String.format(ChatStrings.chatNowChattingWith, receiver.getName()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
