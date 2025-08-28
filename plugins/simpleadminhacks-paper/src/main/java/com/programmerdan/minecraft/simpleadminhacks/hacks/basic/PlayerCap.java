package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCap extends BasicHack implements CommandExecutor {

    public PlayerCap(SimpleAdminHacks plugin, BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin().registerCommand("setplayercap", this);
    }

    @EventHandler
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
        if (e.getResult() != PlayerLoginEvent.Result.KICK_FULL) return;
        if (!e.getPlayer().hasPermission("joinbypass.use")) return;

        e.allow();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender.hasPermission("simpleadmin.setplayercap"))) {
            return false;
        }

        if (args.length < 1) {
            return false;
        }

        int cap;
        try {
            cap = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            return false;
        }
        if (cap < 0) {
            return false;
        }

        Bukkit.getServer().setMaxPlayers(cap);
        sender.sendMessage("Changed player cap to " + cap);
        return true;
    }
}
