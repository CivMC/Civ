package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findTargetedOwnedSnitch;
import static com.untamedears.JukeAlert.util.Utility.doesSnitchExist;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;
import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.entity.Faction;

public class GroupCommand extends PlayerCommand {

    public GroupCommand() {
        super("Group");
        setDescription("Displays information from a group");
        setUsage("/jagroup <group> <page>");
        setArgumentRange(1, 2);
        setIdentifier("jagroup");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            int offset = 1;
            if (args.length > 1) {
                try {
                    offset = Integer.parseInt(args[1]);
                } catch(NumberFormatException e) {
                    offset = 1;
                }
            }
            if (offset < 1) {
                offset = 1;
            }
            if (!sender.hasPermission("jukealert.admin.jagroup")) {
                Faction group = Citadel.getGroupManager().getGroup(args[0]);
                if (group == null) {
                    sender.sendMessage(ChatColor.RED + "That group doesn't exist!");
                    return true;
                }
                String playerName = sender.getName();
                if (!group.isMember(playerName)
                    && !group.isModerator(playerName)
                    && !group.isFounder(playerName))
                {
                    sender.sendMessage(ChatColor.RED + "You are not part of that group!");
                    return true;
                }
            }
            sendLog(sender, args[0], offset);
        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
        }
        return true;
    }

    private void sendLog(CommandSender sender, String group, int offset) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(plugin, group, offset, player);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
    }
}
