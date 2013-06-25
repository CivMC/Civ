package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.isOnSnitch;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;

public class LookupCommand extends PlayerCommand {

    private SnitchManager snitchManager;

    public LookupCommand() {
        super("Lookup");
        setDescription("Lookup a snitch's group by its coordinates");
        setUsage("/jalookup <x> <y> <z> [world]");
        setArgumentRange(3, 4);
        setIdentifier("jalookup");
        snitchManager = plugin.getSnitchManager();
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        boolean canLookupAny = (sender instanceof ConsoleCommandSender || sender.hasPermission("jukealert.admin.lookupany"));
        if (sender instanceof Player || canLookupAny) {
            int x, y, z;
            String world;
            try {
                x = Integer.parseInt(args[0]);
                y = Integer.parseInt(args[1]);
                z = Integer.parseInt(args[2]);
                if(args.length == 3) {
                    world = "world";
                } else {
                    world = args[3];
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "Invalid coordinates.");
                return false;
            }
            if(Bukkit.getWorld(world) == null) {
                sender.sendMessage(ChatColor.RED + "Invalid world.");
                return false;
            }
            Location loc = new Location(Bukkit.getWorld(world), x, y, z);
            Snitch match = snitchManager.getSnitch(loc.getWorld(), loc);
            if(match == null) {
                sender.sendMessage(ChatColor.RED + "You do not own a snitch at those coordinates!");
                return false;
            }
            if(canLookupAny || isOnSnitch(match, sender.getName())) {
                sender.sendMessage(ChatColor.AQUA + "The snitch at [" + x + " " + y + " " + z + "] is owned by " + match.getGroup().getName());
            } else {
                sender.sendMessage(ChatColor.RED + "You do not own a snitch at those coordinates!");               
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You do not own a snitch at those coordinates!");  
            return false;
        }
    }
}
