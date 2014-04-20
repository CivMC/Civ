package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.command.CommandUtils;
import com.untamedears.citadel.command.PlayerCommand;
import com.untamedears.citadel.entity.Faction;

public class GroupStatsCommand extends PlayerCommand {

    public class SendResultsTask implements Runnable {
        public QueryDbTask previousTask;
        public SendResultsTask(QueryDbTask pt) {
            previousTask = pt;
        }
        @Override
        public void run() {
            for (String line : previousTask.results) {
                previousTask.sender.sendMessage(line);
            }
        }
    }

    public class QueryDbTask implements Runnable {
        public CommandSender sender;
        public String groupName;
        public List<String> results = new LinkedList<String>();
        public QueryDbTask(CommandSender s, String gn) {
            sender = s;
            groupName = gn;
        }
        @Override
        public void run() {
            CommandUtils.formatGroupMembers(results, groupName);
            CommandUtils.formatReinforcements(results, groupName,
                CommandUtils.countReinforcements(groupName));
            Bukkit.getScheduler().runTask(
                Citadel.getPlugin(), new SendResultsTask(this));
        }
    }

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
            UUID accountId = player.getUniqueId();
            if (!group.isFounder(accountId) && !group.isModerator(accountId)) {
                sendMessage(sender, ChatColor.RED, "You do not have access to this group's stats");
                return true;
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(
            Citadel.getPlugin(), new QueryDbTask(sender, group_name));
        return true;
    }
}
