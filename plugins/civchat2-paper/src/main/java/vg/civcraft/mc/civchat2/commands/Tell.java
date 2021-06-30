package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;

@CommandAlias("tell|message|m|pm|msg")
public class Tell extends BaseCommand {

	@Syntax("/tell <player> <message>")
	@Description("Sends a private message to someone or enters a private chat with them")
	public void execute(CommandSender sender, @Optional String targetPlayer, @Optional String chatMessage) {
		CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
		Player player = (Player) sender;
		if (targetPlayer.isEmpty() && chatMessage.isEmpty()) {
			UUID chattingWith = chatMan.getChannel(player);
			if (chattingWith != null) {
				chatMan.removeChannel(player);
				player.sendMessage(ChatStrings.chatRemovedFromChat);
			} else {
				player.sendMessage(ChatStrings.chatNotInPrivateChat);
			}
			return;
		}

		Player receiver = Bukkit.getPlayer(targetPlayer);
		if (receiver == null) {
			player.sendMessage(ChatStrings.chatPlayerNotFound);
			return;
		}

		if (!(receiver.isOnline())) {
			player.sendMessage(ChatStrings.chatPlayerIsOffline);
			return;
		}

		if (player == receiver) {
			player.sendMessage(ChatStrings.chatCantMessageSelf);
			return;
		}

		if (!chatMessage.isEmpty()) {
			chatMan.sendPrivateMsg(player, receiver, chatMessage);
			return;
		} else {
			CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
			if (db.isIgnoringPlayer(player.getUniqueId(), receiver.getUniqueId())) {
				player.sendMessage(String.format(ChatStrings.chatNeedToUnignore, receiver.getDisplayName()));
				return;
			}

			if (db.isIgnoringPlayer(receiver.getUniqueId(), player.getUniqueId())) {
				player.sendMessage(ChatStrings.chatPlayerIgnoringYou);
				return;
			}
			chatMan.addChatChannel(player, receiver);
			player.sendMessage(String.format(ChatStrings.chatNowChattingWith, receiver.getDisplayName()));
		}
	}
}
