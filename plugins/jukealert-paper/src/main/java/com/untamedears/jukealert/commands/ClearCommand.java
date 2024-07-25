package com.untamedears.jukealert.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.SnitchLogAppender;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand extends BaseCommand {

    @CommandAlias("jaclear")
    @Description("Deletes all logs a snitch has")
    public void execute(final CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Snitch snitch = JAUtility.findLookingAtOrClosestSnitch(player, JukeAlertPermissionHandler.getClearLogs());
        if (snitch == null) {
            sender.sendMessage(
                ChatColor.RED + "You do not own any snitches nearby or lack permission to delete their logs!");
            return;
        }
        SnitchLogAppender logAppender = (SnitchLogAppender) snitch.getAppender(SnitchLogAppender.class);
        if (logAppender == null) {
            sender.sendMessage(
                ChatColor.RED + "This " + snitch.getType().getName() + " does not keep any logs");
            return;
        }
        logAppender.deleteLogs();
        sender.sendMessage(ChatColor.GREEN + "Deleted all logs for snitch " + JAUtility.genTextComponent(snitch));
    }
}
