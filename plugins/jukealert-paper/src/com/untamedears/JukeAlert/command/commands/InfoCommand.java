package com.untamedears.JukeAlert.command.commands;

import com.untamedears.JukeAlert.JukeAlert;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.tasks.GetSnitchInfoPlayerTask;
import org.bukkit.Bukkit;

public class InfoCommand extends PlayerCommand {

    public InfoCommand() {
        super("Info");
        setDescription("Displays information from a Snitch");
        setUsage("/jainfo <page number>");
        setArgumentRange(0, 1);
        setIdentifier("jainfo");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            int offset = 1;
            if (args.length > 0) {
                offset = Integer.parseInt(args[0]);
            }
            if (offset < 1) {
                offset = 1;
            }
            Set<Snitch> snitches = plugin.getSnitchManager().findSnitches(player.getWorld(), player.getLocation());
            for (Snitch snitch : snitches) {
                //Get only first snitch in cuboid
                if (JukeAlert.isOnSnitch(snitch, player.getName())) {
                    sendLog(sender, snitch, offset);
                    break;
                }
            }
            
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");

        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            return false;
        }
        return false;

    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(plugin, snitch.getId(), offset, player);
        Bukkit.getScheduler().scheduleAsyncDelayedTask(plugin, task);

    }
}
