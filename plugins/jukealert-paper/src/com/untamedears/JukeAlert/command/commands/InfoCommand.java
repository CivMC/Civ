package com.untamedears.JukeAlert.command.commands;

import com.untamedears.JukeAlert.JukeAlert;
import java.util.List;

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
            World world = player.getWorld();

            int offset = 1;
            if (args.length > 0) {
                offset = Integer.parseInt(args[0]);
            }
            if (offset < 1) {
                offset = 1;
            }

            List<Snitch> snitches = plugin.getSnitchManager().getSnitchesByWorld(world);
            for (Snitch snitch : snitches) {
                //Get only first snitch in cuboid
                if (JukeAlert.isOnSnitch(snitch, player.getName())) {
                    if (snitch.isWithinCuboid(player.getLocation())) {
                        sendLog(sender, snitch, offset);
                        return false;
                    }
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
        //List<String> info = task.getInfo();

    }
}
