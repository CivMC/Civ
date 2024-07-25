package com.untamedears.jukealert.commands;

import static com.untamedears.jukealert.util.JAUtility.findLookingAtOrClosestSnitch;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.permission.PermissionType;

import java.util.Objects;

public class NameCommand extends BaseCommand {

    @CommandAlias("janame")
    @Syntax("<name>")
    @Description("Name a snitch")
    public void execute(CommandSender sender, String snitchName) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        Snitch snitch = findLookingAtOrClosestSnitch(player, getPermission());
        if (snitch == null) {
            player.sendMessage(
                ChatColor.RED + "You do not own any snitches nearby or lack permission to view their logs!");
            return;
        }

        renameSnitch(player, snitchName, snitch);
    }

    private static void renameSnitch(Player player, String name, Snitch snitch) {
        String newName = name.length() > 40
            ? name.substring(0, 40)
            : name;

        String prevName = snitch.getName();
        JukeAlert.getInstance().getSnitchManager().renameSnitch(snitch, newName);
        TextComponent lineText = new TextComponent(ChatColor.AQUA + (Objects.equals(prevName, "") ? "Set " : "Changed ") + "snitch name to ");
        lineText.addExtra(JAUtility.genTextComponent(snitch));
        lineText.addExtra(Objects.equals(prevName, "") ? "" : ChatColor.AQUA + " from " + ChatColor.GOLD + prevName);
        player.spigot().sendMessage(lineText);
    }

    private static PermissionType getPermission() {
        return JukeAlertPermissionHandler.getRenameSnitch();
    }
}
