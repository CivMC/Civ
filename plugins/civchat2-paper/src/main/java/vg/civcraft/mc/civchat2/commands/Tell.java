package vg.civcraft.mc.civchat2.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;
import vg.civcraft.mc.civmodcore.command.CivCommand;
import vg.civcraft.mc.civmodcore.command.StandaloneCommand;

@CivCommand(id = "tell")
public class Tell extends StandaloneCommand {

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Player player = (Player) sender;
		if (args.length == 0) {
			UUID chattingWith = chatMan.getChannel(player);
			if (chattingWith != null) {
				chatMan.removeChannel(player);
				player.sendMessage(ChatStrings.chatRemovedFromChat);
			} else {
				player.sendMessage(ChatStrings.chatNotInPrivateChat);
			}
			return true;
		}

		Player receiver = Bukkit.getPlayer(args [0]);
		if (receiver == null) {
			player.sendMessage(ChatStrings.chatPlayerNotFound);
			return true;
		}

		if (!(receiver.isOnline())) {
			player.sendMessage(ChatStrings.chatPlayerIsOffline);
			return true;
		}

		if (player == receiver) {
			player.sendMessage(ChatStrings.chatCantMessageSelf);
			return true;
		}

		if (args.length >= 2) {
			// Player and message
			StringBuilder builder = new StringBuilder();
			for (int x = 1; x < args.length; x++) {
				builder.append(args[x] + " ");
			}

			chatMan.sendPrivateMsg(player, receiver, builder.toString());
			return true;
		} else if (args.length == 1) {
			CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
			if (db.isIgnoringPlayer(player.getUniqueId(), receiver.getUniqueId())) {
				player.sendMessage(String.format(ChatStrings.chatNeedToUnignore, receiver.getDisplayName()));
				return true;
			}

			if (db.isIgnoringPlayer(receiver.getUniqueId(), player.getUniqueId())) {
				player.sendMessage(ChatStrings.chatPlayerIgnoringYou);
				return true;
			}
			chatMan.addChatChannel(player, receiver);
			player.sendMessage(String.format(ChatStrings.chatNowChattingWith, receiver.getDisplayName()));
			return true;
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}
}
