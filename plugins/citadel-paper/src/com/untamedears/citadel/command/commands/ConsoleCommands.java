package com.untamedears.citadel.command.commands;

import static com.untamedears.citadel.Utility.sendMessage;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import com.untamedears.citadel.Citadel;
import com.untamedears.citadel.ConfigManager;
import com.untamedears.citadel.command.ConsoleCommand;
import com.untamedears.citadel.dao.CitadelDao;
import com.untamedears.citadel.dao.CitadelCachingDao;

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
        } else if (command.equalsIgnoreCase("daocachestats")) {
            return GetDaoCacheStats((ConsoleCommandSender)sender, args);
        } else if (command.equalsIgnoreCase("daocachestatsslow")) {
            return GetSlowDaoCacheStats((ConsoleCommandSender)sender, args);
        } else if (command.equalsIgnoreCase("forcecacheflush")) {
            return ForceCacheFlush((ConsoleCommandSender)sender, args);
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
                long maxAge = Long.parseLong(value);
                cm.setCacheMaxAge(maxAge);
                CitadelDao std_dao = Citadel.getDao();
                if (std_dao instanceof CitadelCachingDao) {
                    ((CitadelCachingDao)std_dao).setMaxAge(maxAge);
                }

            } else if (settingName.equalsIgnoreCase("cacheMaxChunks")) {
                int maxChunks = Integer.parseInt(value);
                cm.setCacheMaxChunks(maxChunks);
                CitadelDao std_dao = Citadel.getDao();
                if (std_dao instanceof CitadelCachingDao) {
                    ((CitadelCachingDao)std_dao).setMaxChunks(maxChunks);
                }

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

    public boolean GetDaoCacheStats(ConsoleCommandSender sender, String[] args) {
        CitadelDao std_dao = Citadel.getDao();
        if (!(std_dao instanceof CitadelCachingDao)) {
            sendMessage(sender, ChatColor.RED, "Sorry, the Caching DAO is not being used.");
            return true;
        }
        CitadelCachingDao dao = (CitadelCachingDao)std_dao;
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ChunkCacheSize = %d", dao.getChunkCacheSize()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ChunkCacheLoads = %d", dao.getCounterChunkCacheLoads()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "CacheHits = %d", dao.getCounterCacheHits()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ChunkUnloads = %d", dao.getCounterChunkUnloads()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ChunkTimeouts = %d", dao.getCounterChunkTimeouts()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ReinforcementsSaved = %d", dao.getCounterReinforcementsSaved()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "ReinforcementsDeleted = %d", dao.getCounterReinforcementsDeleted()));
        sendMessage(sender, ChatColor.YELLOW, String.format(
            "PreventedThrashing = %d", dao.getCounterPreventedThrashing()));
        return true;
    }

    public boolean GetSlowDaoCacheStats(ConsoleCommandSender sender, String[] args) {
        CitadelDao std_dao = Citadel.getDao();
        if (!(std_dao instanceof CitadelCachingDao)) {
            sendMessage(sender, ChatColor.RED, "Sorry, the Caching DAO is not being used.");
            return true;
        }
        CitadelCachingDao dao = (CitadelCachingDao)std_dao;
        Map<String, Long> stats = dao.getPendingUpdateCounts();
        for (Map.Entry<String, Long> cursor : stats.entrySet()) {
            sendMessage(sender, ChatColor.YELLOW, String.format(
                        "%s = %d", cursor.getKey(), cursor.getValue()));
        }
        return true;
    }

    public boolean ForceCacheFlush(ConsoleCommandSender sender, String[] args) {
        int flushCount = 5;
        if (args.length >= 2) {
            flushCount = Integer.parseInt(args[1]);
        }
        CitadelDao std_dao = Citadel.getDao();
        if (!(std_dao instanceof CitadelCachingDao)) {
            sendMessage(sender, ChatColor.RED, "Sorry, the Caching DAO is not being used.");
            return true;
        }
        CitadelCachingDao dao = (CitadelCachingDao)std_dao;
        dao.ForceCacheFlush(flushCount);
        sendMessage(sender, ChatColor.YELLOW, "Flush complete.");
        return true;
    }
}
