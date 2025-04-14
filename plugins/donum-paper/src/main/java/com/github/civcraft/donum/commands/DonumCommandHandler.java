package com.github.civcraft.donum.commands;

import com.github.civcraft.donum.commands.commands.Deliver;
import com.github.civcraft.donum.commands.commands.DeliverDeath;
import com.github.civcraft.donum.commands.commands.OpenDeliveries;
import com.github.civcraft.donum.commands.commands.PendingDeliveries;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.commands.CommandManager;


public class DonumCommandHandler extends CommandManager {

    /**
     * Creates a new command manager for Aikar based commands and tab completions.
     *
     * @param plugin The plugin to bind this manager to.
     */
    public DonumCommandHandler(@NotNull Plugin plugin) {
        super(plugin);
    }

    @Override
    public void registerCommands() {
        registerCommand(new OpenDeliveries());
        registerCommand(new Deliver());
        registerCommand(new DeliverDeath());
        registerCommand(new PendingDeliveries());
    }
}
