package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.PlayerCapController;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class PlayerCap extends BasicHack implements CommandExecutor {

    private @Nullable PlayerCapController controller;
    private @Nullable PlayerCapController.Settings settings;

    public PlayerCap(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        plugin().registerCommand("setplayercap", this);
        if (config().getBase().getBoolean("auto.enabled", false)) {
            try {
                enableAutomatic();
            } catch (final IllegalArgumentException exception) {
                plugin().getLogger().warning("Automatic player cap disabled: " + exception.getMessage());
            }
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        controller = null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLoginEvent(final PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL
            && event.getPlayer().hasPermission("joinbypass.use")) {
            event.allow();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onJoin(final PlayerJoinEvent event) {
        if (controller != null) {
            controller.joined(System.nanoTime(), Bukkit.getOnlinePlayers().size());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTick(final ServerTickEndEvent event) {
        if (controller == null) {
            return;
        }
        controller.tick(System.nanoTime(), Bukkit.getOnlinePlayers().size(), event.getTickDuration());
        applyCap("EWMA control");
    }

    private void enableAutomatic() {
        final ConfigurationSection config = config().getBase();
        settings = new PlayerCapController.Settings(
            config.getInt("auto.minCap", 125), config.getInt("auto.maxCap", 200),
            config.getDouble("auto.sampleSeconds", 5), config.getDouble("auto.ewmaHalfLifeSeconds", 30),
            config.getDouble("auto.reduceAboveMspt", 59), config.getDouble("auto.reduceHoldSeconds", 60),
            config.getInt("auto.reduceStep", 1), config.getDouble("auto.increaseBelowMspt", 54),
            config.getDouble("auto.recoverySeconds", 60), config.getDouble("auto.settleSeconds", 90));
        controller = new PlayerCapController(settings, Bukkit.getOnlinePlayers().size(), System.nanoTime());
        applyCap("automatic mode enabled");
    }

    private void applyCap(@NotNull final String reason) {
        if (controller == null || Bukkit.getMaxPlayers() == controller.getCap()) {
            return;
        }
        final int previous = Bukkit.getMaxPlayers();
        Bukkit.getServer().setMaxPlayers(controller.getCap());
        plugin().getLogger().info(String.format(Locale.ROOT,
            "Player cap %d -> %d (%s; online=%d, MSPT=%.2f, EWMA=%.2f ms)", previous, controller.getCap(),
            reason, Bukkit.getOnlinePlayers().size(), controller.getSampleMspt(), controller.getSmoothedMspt()));
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender, @NotNull final Command command,
                             @NotNull final String label, @NotNull final String @NotNull [] args) {
        if (!(sender.hasPermission("simpleadmin.setplayercap"))) {
            return false;
        }

        if (args.length != 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("status")) {
            final String status = controller == null ? "Manual mode; cap=" + Bukkit.getMaxPlayers()
                : String.format(Locale.ROOT,
                    "Automatic mode; cap=%d, bounds=%d–%d, MSPT=%.2f, EWMA=%.2f ms, healthy=%.0f/%.0fs, settling=%.0fs",
                    controller.getCap(), settings.minCap(), settings.maxCap(), controller.getSampleMspt(),
                    controller.getSmoothedMspt(), controller.getHealthySeconds(), settings.recoverySeconds(),
                    controller.getSettlingRemaining(System.nanoTime()));
            sender.sendMessage(Component.text(status + "; online=" + Bukkit.getOnlinePlayers().size(), NamedTextColor.YELLOW));
            return true;
        }
        if (args[0].equalsIgnoreCase("auto")) {
            try {
                enableAutomatic();
                sender.sendMessage(Component.text("Automatic player cap enabled.", NamedTextColor.GREEN));
            } catch (final IllegalArgumentException exception) {
                sender.sendMessage(Component.text(exception.getMessage(), NamedTextColor.RED));
            }
            return true;
        }

        final int cap;
        try {
            cap = Integer.parseInt(args[0]);
        } catch (final NumberFormatException exception) {
            return false;
        }
        if (cap < 0) {
            return false;
        }

        controller = null;
        Bukkit.getServer().setMaxPlayers(cap);
        sender.sendMessage(Component.text("Changed player cap to " + cap + " (manual mode).", NamedTextColor.GREEN));
        return true;
    }
}
