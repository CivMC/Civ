package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;
import static com.untamedears.citadel.Utility.toAccountId;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.GroupManager;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

/**
 * User: JonnyD
 * Date: 7/18/12
 * Time: 11:57 PM
 */
public class TransferCommand extends PlayerCommand {

    public TransferCommand() {
        super("Transfer Group");
        setDescription("Transfers a group to another player. WARNING: You lose reinforcements associated with this group. This cannot be undone.");
        setUsage("/cttransfer §8<group-name> <player-name>");
        setArgumentRange(2, 2);
        setIdentifiers(new String[] {"cttransfer", "ctt"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        final String groupName = args[0];
        final GroupManager groupManager = Citadel.getGroupManager();
        final Faction group = groupManager.getGroup(groupName);
        if(group == null){
            sendMessage(sender, ChatColor.RED, "Group doesn't exist");
            return true;
        }
        Player player = null;
        UUID accountId = null;
        String senderName = null;
        if (sender instanceof Player) {
            player = (Player)sender;
            accountId = player.getUniqueId();
            senderName = player.getDisplayName();
        }
        final String targetName = args[1];
        final UUID targetAccountId = toAccountId(targetName);
        if (targetAccountId == null) {
            sendMessage(sender, ChatColor.RED, "Player does not exist");
            return true;
        }
        final Player targetPlayer = Bukkit.getPlayer(targetAccountId);
        final boolean adminMode = sender.hasPermission("citadel.admin.cttransfer");
        if (!adminMode) {
            if (group.isDisciplined()) {
                sendMessage(sender, ChatColor.RED, Faction.kDisciplineMsg);
                return true;
            }
            if (!group.isFounder(accountId)) {
                sendMessage(sender, ChatColor.RED, "Invalid permission to transfer this group");
                return true;
            }
            if (accountId != null && accountId.equals(targetAccountId)) {
                sendMessage(sender, ChatColor.RED, "You already own this group");
                return true;
            }
            final int groupsAllowed = Citadel.getConfigManager().getGroupsAllowed();
            if (groupManager.getPlayerGroupsAmount(targetAccountId) >= groupsAllowed) {
                sendMessage(sender, ChatColor.RED, "This player has already reached the maximum amount of groups allowed");
                return true;
            }
            if (targetPlayer == null) {
                sendMessage(sender, ChatColor.RED, "User must be online");
                return true;
            }
        } else { // if adminMode
            if (group.isFounder(targetAccountId)) {
                sendMessage(sender, ChatColor.RED, "Player already owns this group");
                return true;
            }
        }
        if (group.isPersonalGroup()) {
            sendMessage(sender, ChatColor.RED, "You cannot transfer a personal group");
            return true;
        }
        if (group.isMember(targetAccountId)) {
            groupManager.removeMemberFromGroup(groupName, targetAccountId, player);
        }
        if (group.isModerator(targetAccountId)) {
            groupManager.removeModeratorFromGroup(groupName, targetAccountId, player);
        }
        group.setFounderId(targetAccountId);
        groupManager.addGroup(group, player);
        sendMessage(sender, ChatColor.GREEN, "You have transferred %s to %s", groupName, targetName);
        if(targetPlayer != null){
            sendMessage(targetPlayer, ChatColor.YELLOW, "%s has transferred the group %s to you", 
                    senderName == null ? "ADMIN CONSOLE" : senderName, groupName);
        }
        return true;
    }
}
