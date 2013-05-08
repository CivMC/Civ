package com.untamedears.JukeAlert.command.commands;

import com.untamedears.JukeAlert.JukeAlert;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;
import com.untamedears.JukeAlert.chat.ChatFiller;

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
                        break;
                    }
                }
            }

        } else {
            sender.sendMessage(ChatColor.RED + "You do not own any snitches nearby!");
            return false;
        }
        return false;

    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset) {
        Player player = (Player) sender;
        List<String> info = plugin.getJaLogger().getSnitchInfo(snitch.getId(), 10 * offset);

        player.sendMessage(ChatColor.WHITE + " Snitch Log " + ChatColor.DARK_GRAY + "----------------------------------------");
        player.sendMessage(ChatColor.GRAY + String.format("  %s %s %s", ChatFiller.fillString("Name", (double) 25), ChatFiller.fillString("Reason", (double) 20), ChatFiller.fillString("Details", (double) 30)));
        if (info != null) {
            for (String dataEntry : info) {
                player.sendMessage(dataEntry);
            }
        } else {
            player.sendMessage(ChatColor.AQUA + "Page " + offset + " is empty");
        }
    }
}
