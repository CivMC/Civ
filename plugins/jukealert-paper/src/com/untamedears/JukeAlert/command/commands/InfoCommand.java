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

public class InfoCommand extends PlayerCommand {

    public InfoCommand() {
        super("Info");
        setDescription("Displays information from a Snitch");
        setUsage("/jainfo <page number> [censor]");
        setArgumentRange(0, 2);
        setIdentifier("jainfo");
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            int offset = 1;
            if (args.length > 0) {
            	try {
            		offset = Integer.parseInt(args[0]);
            	} catch(NumberFormatException e) {
            		offset = 1;
            	}
            }
            if (offset < 1) {
                offset = 1;
            }
            Snitch snitch = findTargetedOwnedSnitch((Player) sender);
            if (snitch != null) {
                sendLog(sender, snitch, offset, args.length == 2);
            } else {
                sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            }
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + " You do not own any snitches nearby!");
            return false;
        }
    }

    private void sendLog(CommandSender sender, Snitch snitch, int offset, boolean shouldCensor) {
        Player player = (Player) sender;
        GetSnitchInfoPlayerTask task = new GetSnitchInfoPlayerTask(plugin, snitch.getId(), offset, player, shouldCensor);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, task);

    }
}
