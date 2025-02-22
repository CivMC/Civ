package vg.civcraft.mc.civchat2.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;
import vg.civcraft.mc.civchat2.database.CivChatDAO;


public class Tell extends BaseCommand {

    @CommandAlias("tell|message|m|pm|msg")
    @Syntax("[player] [message]")
    @Description("Sends a private message to someone or enters a private chat with them")
    @CommandCompletion("@allplayers @nothing")
    public void execute(Player player, @Optional String targetPlayer, @Optional String chatMessage) {
        CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
        if (targetPlayer == null && chatMessage == null) {
            UUID chattingWith = chatMan.getChannel(player);
            if (chattingWith != null) {
                chatMan.removeChannel(player);
                player.sendRichMessage(ChatStrings.chatRemovedFromChat);
            } else {
                player.sendRichMessage(ChatStrings.chatNotInPrivateChat);
            }
            return;
        }

        Player receiver = Bukkit.getPlayer(targetPlayer);
        if (receiver == null) {
            player.sendRichMessage(ChatStrings.chatPlayerNotFound);
            return;
        }

        if (!(receiver.isOnline())) {
            player.sendRichMessage(ChatStrings.chatPlayerIsOffline);
            return;
        }

        if (player == receiver) {
            player.sendRichMessage(ChatStrings.chatCantMessageSelf);
            return;
        }

        if (!(chatMessage == null)) {
            chatMan.sendPrivateMsg(player, receiver, chatMessage);
            return;
        } else {
            CivChatDAO db = CivChat2.getInstance().getDatabaseManager();
            if (db.isIgnoringPlayer(player.getUniqueId(), receiver.getUniqueId())) {
                player.sendRichMessage(String.format(ChatStrings.chatNeedToUnignore, receiver.getDisplayName()));
                return;
            }

            if (db.isIgnoringPlayer(receiver.getUniqueId(), player.getUniqueId())) {
                player.sendRichMessage(ChatStrings.chatPlayerIgnoringYou);
                return;
            }
            chatMan.addChatChannel(player, receiver);
            player.sendRichMessage(String.format(ChatStrings.chatNowChattingWith, receiver.getDisplayName()));
        }
    }
}
