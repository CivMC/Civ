package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ReinforcedChestBreakConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

import java.util.List;
import java.util.UUID;

/**
 * Sends every 3 minutes a message to the admins if a chest is broken
 */
public class ReinforcedChestBreak extends SimpleHack<ReinforcedChestBreakConfig> implements Listener {

    public static String NAME = "ReinforcedChestBreak";

    private ReinforcementManager manager;
    private Messenger messenger;
    private String lastMessage;
    private List<Player> messageList;

    public ReinforcedChestBreak(SimpleAdminHacks plugin, ReinforcedChestBreakConfig config) {
        super(plugin, config);
    }

    @Override
    public void registerListeners() {
        Bukkit.getPluginManager().registerEvents(this, plugin());
    }

    @Override
    public void registerCommands() {}

    @Override
    public void dataBootstrap() {
        for (UUID uniqueId: config.getAdmins())
        {
            messageList.add(Bukkit.getPlayer(uniqueId));
        }

        lastMessage = "";

        manager = Citadel.getReinforcementManager();
        messenger = new Messenger();

        Bukkit.getScheduler().runTaskTimer(plugin(), messenger, 0, config.getDelay() * 20);
    }

    @Override
    public void unregisterListeners() {}

    @Override
    public void unregisterCommands() {}

    @Override
    public void dataCleanup() {}

    @Override
    public String status() {
        return "Delay: " + config.getDelay() +
                ", next message to be send: " + lastMessage;
    }

    /**
     * Gets fired by the BLockBreakEvent and checks if the block is reinforced
     * @param eve BLockBreakEvent
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent eve)
    {
        if(manager.isReinforced(eve.getBlock()))
        {
            String name = eve.getPlayer().getDisplayName();
            Location loc = eve.getBlock().getLocation();

            lastMessage = setVars(name,
                    String.valueOf(loc.getBlockX()),
                    String.valueOf(loc.getBlockY()),
                    String.valueOf(loc.getBlockZ()));
        }
    }

    /**
     * Builds the message
     * @param name the player name
     * @param x block x
     * @param y block y
     * @param z block z
     * @return returns the builded String
     */
    private String setVars(String name, String x, String y, String z)
    {
        return ChatColor.translateAlternateColorCodes('&', config.getMessage()
                                                 .replace("%player%", name)
                                                 .replace("%x%", x)
                                                 .replace("%y%", y)
                                                 .replace("%z%", z));
    }

    private class Messenger implements Runnable {

        @Override
        public void run() {
            for (UUID uuid: config.getAdmins())
            {
                Bukkit.getPlayer(uuid).sendMessage(lastMessage);
            }
        }
    }
}
