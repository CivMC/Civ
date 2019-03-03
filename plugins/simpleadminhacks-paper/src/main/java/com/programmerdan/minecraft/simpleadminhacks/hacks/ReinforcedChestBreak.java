package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ReinforcedChestBreakConfig;

import vg.civcraft.mc.citadel.Citadel;
import vg.civcraft.mc.citadel.ReinforcementManager;

/**
 * Sends every 3 minutes a message to the admins if a chest is broken
 */
public class ReinforcedChestBreak extends SimpleHack<ReinforcedChestBreakConfig> implements Listener {

    public static String NAME = "ReinforcedChestBreak";

    private ReinforcementManager manager;
    private Messenger messenger;
    private Set<String> messages;

    public ReinforcedChestBreak(SimpleAdminHacks plugin, ReinforcedChestBreakConfig config) {
        super(plugin, config);
    }

	public static ReinforcedChestBreakConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new ReinforcedChestBreakConfig(plugin, config);
	}

    @Override
    public void registerListeners() {
    	if (!config.isEnabled()) return;
        Bukkit.getPluginManager().registerEvents(this, plugin());
    }

    @Override
    public void registerCommands() {}

    @Override
    public void dataBootstrap() {
    	if (!config.isEnabled()) return;
        messages = new HashSet<>();

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
        return "Delay: " + config.getDelay();
    }

    /**
     * Gets fired by the BLockBreakEvent and checks if the block is reinforced
     * @param eve BLockBreakEvent
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled=true)
    public void onBlockBreak(BlockBreakEvent eve) {
    	if (!config.isEnabled()) return;
    	if (eve.getPlayer() == null) return;
    	if (eve.getBlock() == null) return;
    	Material bbe = eve.getBlock().getType();
		if (bbe == null) return;
		if (manager == null) manager = Citadel.getReinforcementManager();
        if (Material.CHEST.equals(bbe) || Material.TRAPPED_CHEST.equals(bbe)
        		|| Material.ENDER_CHEST.equals(bbe) || Material.FURNACE.equals(bbe)
        		|| Material.BURNING_FURNACE.equals(bbe) || Material.DISPENSER.equals(bbe)
        		|| Material.DROPPER.equals(bbe) || Material.HOPPER.equals(bbe)) {
            if(manager.isReinforced(eve.getBlock())) {
                String name = eve.getPlayer().getDisplayName();
                Location loc = eve.getBlock().getLocation();

                String msg = setVars(name,
                                    String.valueOf(loc.getBlockX()),
                                    String.valueOf(loc.getBlockY()),
                                    String.valueOf(loc.getBlockZ()));

                if(messages.add(msg)) {
                    plugin().log(Level.INFO, msg);
                }
            }
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
    private String setVars(String name, String x, String y, String z) {
        return ChatColor.translateAlternateColorCodes('&', config.getMessage()
                                                 .replace("%player%", name)
                                                 .replace("%x%", x)
                                                 .replace("%y%", y)
                                                 .replace("%z%", z));
    }

    private class Messenger implements Runnable {

        @Override
        public void run() {
            for (String message: messages) {
                plugin().serverOperatorBroadcast(message);
            }
            messages.clear();
        }
    }
}
