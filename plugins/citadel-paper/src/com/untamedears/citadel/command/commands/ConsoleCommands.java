package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ConfigManager;
import com.untamedears.citadel.command.ConsoleCommand;

public class ConsoleCommands extends ConsoleCommand {
    public ConsoleCommands() {
        super("Console Commands");
        setDescription("Handles Console Commands");
        setUsage("ctcon");
        setArgumentRange(1,100);
        setIdentifiers(new String[] {"ctcon"});
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ConsoleCommandSender)) {
            return true;
        }
        String command = args[0];
        if (command.equalsIgnoreCase("getconfig")) {
            return GetConfig((ConsoleCommandSender)sender, args);
        } else if (command.equalsIgnoreCase("setconfig")) {
            return SetConfig((ConsoleCommandSender)sender, args);
        }
        return false;
    }

    public boolean GetConfig(ConsoleCommandSender sender, String[] args) {
        if (args.length < 2) {
            sendMessage(sender, ChatColor.RED, "Specify setting");
            return true;
        }
        String settingName = args[1];
        ConfigManager cm = Citadel.getConfigManager();
        if (settingName.equalsIgnoreCase("flashLength")) {
            sendMessage(sender, ChatColor.YELLOW, "flashLength == " + cm.getFlashLength());

        } else if (settingName.equalsIgnoreCase("autoModeReset")) {
            sendMessage(sender, ChatColor.YELLOW, "autoModeReset == " + cm.getAutoModeReset());

        } else if (settingName.equalsIgnoreCase("verboseLogging")) {
            sendMessage(sender, ChatColor.YELLOW, "verboseLogging == " + cm.getVerboseLogging());

        } else if (settingName.equalsIgnoreCase("redstoneDistance")) {
            sendMessage(sender, ChatColor.YELLOW, "redstoneDistance == " + cm.getRedstoneDistance());

        } else if (settingName.equalsIgnoreCase("groupsAllowed")) {
            sendMessage(sender, ChatColor.YELLOW, "groupsAllowed == " + cm.getGroupsAllowed());

        } else if (settingName.equalsIgnoreCase("cacheMaxAge")) {
            sendMessage(sender, ChatColor.YELLOW, "cacheMaxAge == " + cm.getCacheMaxAge());

        } else if (settingName.equalsIgnoreCase("cacheMaxChunks")) {
            sendMessage(sender, ChatColor.YELLOW, "cacheMaxChunks == " + cm.getCacheMaxChunks());

        } else {
            sendMessage(sender, ChatColor.RED, "Unknown setting: " + settingName);
        }
        return true;
    }

    public boolean SetConfig(ConsoleCommandSender sender, String[] args) {
        if (args.length < 3) {
            sendMessage(sender, ChatColor.RED, "Specify setting and new value");
            return true;
        }
        boolean success = true;
        String settingName = args[1];
        String value = args[2];
        ConfigManager cm = Citadel.getConfigManager();
        try {
            if (settingName.equalsIgnoreCase("flashLength")) {
                cm.setFlashLength(Integer.parseInt(value));

            } else if (settingName.equalsIgnoreCase("autoModeReset")) {
                cm.setAutoModeReset(Integer.parseInt(value));

            } else if (settingName.equalsIgnoreCase("verboseLogging")) {
                cm.setVerboseLogging(Boolean.parseBoolean(value));

            } else if (settingName.equalsIgnoreCase("redstoneDistance")) {
                cm.setRedstoneDistance(Double.parseDouble(value));

            } else if (settingName.equalsIgnoreCase("groupsAllowed")) {
                cm.setGroupsAllowed(Integer.parseInt(value));

            } else if (settingName.equalsIgnoreCase("cacheMaxAge")) {
                cm.setCacheMaxAge(Long.parseLong(value));

            } else if (settingName.equalsIgnoreCase("cacheMaxChunks")) {
                cm.setCacheMaxChunks(Integer.parseInt(value));

            } else {
                sendMessage(sender, ChatColor.RED, "Unknown setting: " + settingName);
                success = false;
            }
        } catch (Exception ex) {
            sendMessage(sender, ChatColor.RED, "Invalid setting value: " + ex.getMessage());
            success = false;
        }
        if (success) {
            sendMessage(sender, ChatColor.GREEN, "Setting updated");
        }
        return true;
    }
}
