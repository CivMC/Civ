package com.programmerdan.minecraft.banstick.commands;

import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.util.Set;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GetAltsCommand implements CommandExecutor {

    public static String name = "getalts";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "You must specify a single player name or uuid");
            return false;
        }
        UUID uuid = UntangleCommand.resolveName(args [0]);
        if (uuid == null) {
            sender.sendMessage(ChatColor.RED + "Could not parse player: " + args [0]);
            return false;
        }
        BSPlayer player = BSPlayer.byUUID(uuid);
        if (player == null) {
            sender.sendMessage(ChatColor.RED + "Player with uuid is not known: " + uuid.toString());
            return false;
        }
        Set<BSPlayer> directAssoc = player.getTransitiveSharedPlayers(true);
        Set<BSPlayer> ignoredAssoc = player.getTransitiveSharedPlayers(false);
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GOLD + "Directly associated accounts for " + player.getName() + " are: ");
        for(BSPlayer alt : directAssoc) {
            sb.append(alt.getName());
            sb.append("  ");
        }
        sender.sendMessage(sb.toString());
        sb = new StringBuilder();
        sb.append(ChatColor.GOLD + "Associated accounts split off through exclusions are: ");
        for(BSPlayer alt : ignoredAssoc) {
            if (directAssoc.contains(alt)) {
                continue;
            }
            sb.append(alt.getName());
            sb.append("  ");
        }
        sender.sendMessage(sb.toString());
        return true;
    }


}
