package net.civmc.announcements.update;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import java.time.Duration;
import java.time.Instant;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import net.civmc.announcements.AnnouncementsPlugin;
import net.civmc.announcements.TimeComponent;
import net.kyori.adventure.text.Component;

public class UpdateListener {

    private final ProxyServer server;
    private final AnnouncementsPlugin plugin;
    private final Component text;
    private final Component kick;

    private volatile Instant restart;
    private volatile boolean block;

    private volatile ScheduledTask task;

    public UpdateListener(ProxyServer proxy, AnnouncementsPlugin plugin, Component text, Component kick) {
        this.server = proxy;
        this.plugin = plugin;
        this.text = text;
        this.kick = kick;
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
        }
        this.restart = null;
        this.block = false;
    }

    public boolean isRestarting() {
        return this.restart != null;
    }

    public void setRestart(Instant restart, boolean block) {
        this.restart = restart;
        this.block = block;
        Duration duration = Duration.between(Instant.now(), restart);
        if (this.block) {
            task = server.getScheduler().buildTask(plugin, () -> {
                for (Player player : server.getAllPlayers()) {
                    if (!player.hasPermission("civannouncements.bypass")) {
                        player.disconnect(kick);
                    }
                }
            }).delay(duration.plusSeconds(1)).schedule();
        }
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(TimeComponent.replace(text, duration));
        }
    }

    @Subscribe
    public void on(LoginEvent event) {
        if (restart == null) {
            return;
        }
        Duration duration = Duration.between(Instant.now(), restart);
        if (!duration.isNegative()) {
            server.getScheduler().buildTask(plugin,
                    () -> event.getPlayer().sendMessage(TimeComponent.replace(text, duration)))
                .delay(Duration.ofSeconds(3)).schedule();
            return;
        }
        if (block && !event.getPlayer().hasPermission("civannouncements.bypass")) {
            event.setResult(ResultedEvent.ComponentResult.denied(kick));
        }
    }
}
