package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

public class GroupStatsCommand extends PlayerCommand {

    public GroupStatsCommand() {
        super("View Group Stats");
        setDescription("View citadel group stats");
        setUsage("/ctgstats <group-name>");
        setArgumentRange(1, 1);
        setIdentifiers(new String[] {"ctgstats", "ctgst"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        String group_name = args[0];
        Faction group = Citadel.getGroupManager().getGroup(group_name);
        if (group == null) {
            sendMessage(sender, ChatColor.RED, "Group not found");
            return true;
        }
        if (sender instanceof Player && !sender.hasPermission("citadel.admin.ctgstats")) {
            Player player = (Player)sender;
            String player_name = player.getName();
            if (!player_name.equals(group.getFounder()) && !group.isModerator(player_name)) {
                sendMessage(sender, ChatColor.RED, "You do not have access to this group's stats");
                return true;
            }
        }
        CommandUtils.printGroupMembers(sender, group_name);
        CommandUtils.printReinforcements(
            sender, group_name, CommandUtils.countReinforcements(group_name));
        return true;
    }
}
