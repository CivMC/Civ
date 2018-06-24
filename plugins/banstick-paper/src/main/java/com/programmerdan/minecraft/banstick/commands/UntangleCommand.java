package com.programmerdan.minecraft.banstick.commands;

import com.programmerdan.minecraft.banstick.data.BSExclusion;
import com.programmerdan.minecraft.banstick.data.BSPlayer;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.NameAPI;

public class UntangleCommand implements CommandExecutor {

    public static String name = "untangle";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "You must specify player names or uuids");
            return true;
        }
        Set<BSPlayer> subGraphPlayers = new HashSet<>();
        Set<BSPlayer> allGraphPlayers = new HashSet<>();
        // parse uuids out of the command
        for (String arg : args) {
            UUID uuid = resolveName(arg);
            if (uuid == null) {
                sender.sendMessage(ChatColor.RED + "Could not parse player: " + arg);
                return false;
            }
            subGraphPlayers.add(BSPlayer.byUUID(uuid));
        }
        // Determine all nodes (players) in the graph (association network) we are creating a subgraph off
        for (BSPlayer player : subGraphPlayers) {
            allGraphPlayers.addAll(player.getTransitiveSharedPlayers(true));
        }

        int delCounter = 0;
        // Delete all preexisting exclusions within this graph
        for (BSPlayer playerOuter : allGraphPlayers) {
            for (BSPlayer playerInner : allGraphPlayers) {
                BSExclusion excl = playerOuter.getExclusionWith(playerInner);
                if (excl != null) {
                    excl.delete();
                    delCounter++;
                }
            }
        }

        int createCounter = 0;
        // create exclusions between all players in the new subgraph and all players not in the new subgraph
        Set<BSPlayer> outsideSubGraphPlayers = new HashSet<>(allGraphPlayers);
        outsideSubGraphPlayers.removeAll(subGraphPlayers);
        for (BSPlayer inside : subGraphPlayers) {
            for (BSPlayer outside : outsideSubGraphPlayers) {
                BSExclusion excl = BSExclusion.create(inside, outside);
                inside.addExclusion(excl);
                outside.addExclusion(excl);
                createCounter++;
            }
        }
        sender.sendMessage(ChatColor.GREEN + String.format(
                "Added exclusions to group containing %d players. %d exclusions were created and %d exclusions were deleted",
                allGraphPlayers.size(), createCounter, delCounter));
        StringBuilder sb = new StringBuilder();
        for(BSPlayer player : subGraphPlayers) {
            sb.append(player.getName());
            sb.append(":");
            sb.append(player.getUUID());
            sb.append("  ");
        }
        sender.sendMessage(ChatColor.GREEN + String.format("First group created contains %d players: %s", subGraphPlayers.size(), sb.toString()));

        sb = new StringBuilder();
        for(BSPlayer player : outsideSubGraphPlayers) {
            sb.append(player.getName());
            sb.append(":");
            sb.append(player.getUUID());
            sb.append("  ");
        }
        sender.sendMessage(ChatColor.GREEN + String.format("Second group created contains %d players: %s", outsideSubGraphPlayers.size(), sb.toString()));
        return true;
    }

    public static UUID resolveName(String input) {
        UUID playerId = null;
        if (input.length() <= 16) {
            // interpret as player name
            try {
                return NameAPI.getUUID(input);
            } catch (NoClassDefFoundError ncde) {
            }
            if (playerId == null) {
                Player match = Bukkit.getPlayer(input);
                if (match != null) {
                    return match.getUniqueId();
                } else {
                    OfflinePlayer offPlay = Bukkit.getOfflinePlayer(input);
                    if (offPlay != null) {
                        return offPlay.getUniqueId();
                    }
                }
            }
        } else if (input.length() == 36) {
            try {
                playerId = UUID.fromString(input);
            } catch (IllegalArgumentException iae) {
                return null;
            }
        }
        return null;
    }

}
