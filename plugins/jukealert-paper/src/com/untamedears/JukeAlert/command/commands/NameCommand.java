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

public class NameCommand extends PlayerCommand {

    public NameCommand() {
        super("Name");
        setDescription("Set snitch name");
        setUsage("/janame <name>");
        setArgumentRange(1, 1);
        setIdentifier("janame");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            World world = player.getWorld();

            String name = "";
            if (args[0].length() > 40) {
                name = args[0].substring(0, 39);
            } else {
            	name = args[0];
            }
            
            Set<Snitch> snitches = plugin.getSnitchManager().findSnitches(player.getWorld(), player.getLocation());
            for (Snitch snitch : snitches) {
                //Get only first snitch in cuboid
                if (JukeAlert.isOnSnitch(snitch, player.getName())) {
                   plugin.getJaLogger().updateSnitchName(snitch, name);
                   Snitch newSnitch = snitch;
                   newSnitch.setName(name);
                   plugin.getSnitchManager().removeSnitch(snitch);
                   plugin.getSnitchManager().addSnitch(newSnitch);
                   sender.sendMessage(ChatColor.AQUA + " Changed snitch name to " + name);
                   break;
                }
            }

        } else {
            sender.sendMessage(ChatColor.RED + "You do not own any snitches nearby!");
            return false;
        }
        return false;

    }
}
