package com.untamedears.jukealert.commands;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.annotation.Syntax;
import com.untamedears.jukealert.JukeAlert;
import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.util.JAUtility;
import com.untamedears.jukealert.util.JukeAlertPermissionHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.namelayer.permission.PermissionType;

public class NameAtCommand extends BaseCommand {

    private record ParsedArgs(Location location, String snitchName) {

    }

    @CommandAlias("janameat")
    @Syntax("[world] <x> <y> <z> <name>")
    @Description("Name a snitch at the given location")
    public void execute(CommandSender sender, String[] args) throws InvalidCommandArgument {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be run by players", NamedTextColor.RED));
            return;
        }
        ParsedArgs parsed = parseArgs(player, args);
        renameSnitch(player, parsed.snitchName, parsed.location);
    }

    private static ParsedArgs parseArgs(Player player, String[] args) throws InvalidCommandArgument {
        // Need at least 3 coordinates and a name.
        if (args.length < 4) {
            throw new InvalidCommandArgument("Not enough arguments.");
        }

        World world;
        int xIndex;
        int yIndex;
        int zIndex;
        // If the first arg is not a number, then it must be the world.
        if (!isInteger(args[0])) {
            String worldName = args[0];
            world = Bukkit.getWorld(worldName);
            if (world == null) {
                throw new InvalidCommandArgument("Unknown world: " + worldName + ".");
            }
            xIndex = 1;
            yIndex = 2;
            zIndex = 3;

            // The world was provided, so we need at least 5 args now.
            if (args.length < 5) {
                throw new InvalidCommandArgument("Not enough arguments.");
            }
        } else {
            world = player.getLocation().getWorld();
            xIndex = 0;
            yIndex = 1;
            zIndex = 2;
        }

        int x;
        int y;
        int z;
        try {
            x = Integer.parseInt(args[xIndex]);
            y = Integer.parseInt(args[yIndex]);
            z = Integer.parseInt(args[zIndex]);
        } catch (NumberFormatException e) {
            throw new InvalidCommandArgument(
                String.format("Coordinates must be numbers: %s %s %s.", args[xIndex], args[yIndex], args[zIndex]));
        }

        String snitchName = Stream.of(args).skip(zIndex + 1).collect(Collectors.joining(" "));

        return new ParsedArgs(new Location(world, x, y, z), snitchName);
    }

    private static boolean isInteger(String arg) {
        try {
            Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    private static void renameSnitch(Player player, String name, Location location) {
        Snitch snitch = JukeAlert.getInstance().getSnitchManager().getSnitchAt(location);
        if (snitch == null || !snitch.hasPermission(player, getPermission())) {
            player.sendMessage(
                ChatColor.RED + "You do not own a snitch at those coordinates or lack permission to rename it!");
            return;
        }

        String newName = name.length() > 40
            ? name.substring(0, 40)
            : name;

        String prevName = snitch.getName();
        JukeAlert.getInstance().getSnitchManager().renameSnitch(snitch, newName);
        TextComponent lineText = new TextComponent(ChatColor.AQUA + " Changed snitch name to ");
        lineText.addExtra(JAUtility.genTextComponent(snitch));
        lineText.addExtra(ChatColor.AQUA + " from " + ChatColor.GOLD + prevName);
        player.spigot().sendMessage(lineText);
    }

    private static PermissionType getPermission() {
        return JukeAlertPermissionHandler.getRenameSnitch();
    }
}
