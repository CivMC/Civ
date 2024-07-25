package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.appender.LeverToggleAppender;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class ToggleLeverCommand extends BaseCommand {

    @CommandAlias("jatogglelevers")
    @Description("Toggles flag that indicates if a juke should trigger a lever.")
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        if (args.length >= 1) {
            return;
        }
        PermissionType togglePerm = JukeAlertPermissionHandler.getToggleLevers();
        Snitch snitch = findLookingAtOrClosestSnitch(player, togglePerm);
        if (snitch == null) {
            player.sendMessage(
                org.bukkit.ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
            return;
        }
        if (!snitch.hasAppender(LeverToggleAppender.class)) {
            player.sendMessage(ChatColor.RED + "This snitch does not have Toggle lever functionality!");
            return;
        }
        LeverToggleAppender toggleAppender = (LeverToggleAppender) snitch.getAppender(LeverToggleAppender.class);
        toggleAppender.switchState();
        player.sendMessage(ChatColor.GREEN + "Toggled lever activation " + (toggleAppender.shouldToggle() ? "on" : "off"));
    }
}
