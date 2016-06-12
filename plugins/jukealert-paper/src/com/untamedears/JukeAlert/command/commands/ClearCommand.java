package com.untamedears.JukeAlert.command.commands;

import static com.untamedears.JukeAlert.util.Utility.findLookingAtOrClosestSnitch;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.untamedears.JukeAlert.command.PlayerCommand;
import com.untamedears.JukeAlert.model.Snitch;

import org.bukkit.Bukkit;

import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ClearCommand extends PlayerCommand {

    public ClearCommand() {
        super("Clear");
        setDescription("Clears snitch logs");
        setUsage("/jaclear");
        setArgumentRange(0, 0);
        setIdentifier("jaclear");
    }

    @Override
    public boolean execute(final CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            final Snitch snitch = findLookingAtOrClosestSnitch(player, PermissionType.getPermission("CLEAR_SNITCHLOG"));
            if (snitch != null) {
                Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        deleteLog(sender, snitch);
                    }
                });
               return true;
            }
            else {
            	sender.sendMessage(ChatColor.RED + "You do not own any snitches nearby or lack permission to delete their logs!");
            	return true;
            }
        } else {
            sender.sendMessage("You must be a player!");
            return false;
        }
    }

    private void deleteLog(CommandSender sender, Snitch snitch) {
        Player player = (Player) sender;
        Boolean completed = plugin.getJaLogger().deleteSnitchInfo(snitch.getId());

        if (completed) {
            player.sendMessage(ChatColor.AQUA + "Snitch Cleared");
        } else {
            player.sendMessage(ChatColor.DARK_RED + "Snitch Clear Failed");
        }
    }
}
