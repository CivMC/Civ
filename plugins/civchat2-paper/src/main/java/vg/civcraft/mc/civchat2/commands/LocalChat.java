package vg.civcraft.mc.civchat2.commands;

import static vg.civcraft.mc.civchat2.ChatStrings.localChatFormat;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import java.util.HashSet;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civchat2.ChatStrings;
import vg.civcraft.mc.civchat2.CivChat2;
import vg.civcraft.mc.civchat2.CivChat2Manager;

public class LocalChat extends BaseCommand {

    @CommandAlias("local|localchat")
    @Description("Enter local chat")

    public void execute(CommandSender sender, @Optional String chatMessage) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        CivChat2Manager chatMan = CivChat2.getInstance().getCivChat2Manager();
        Set<Player> players = new HashSet<>(CivChat2.getInstance().getServer().getOnlinePlayers());

        if (chatMessage == null) {
            chatMan.removeChannel(player);
            chatMan.removeGroupChat(player);
            player.sendMessage(ChatStrings.chatMovedToGlobal);
            return;
        }
        chatMan.broadcastMessage(player, chatMessage, localChatFormat, players);
    }
}
