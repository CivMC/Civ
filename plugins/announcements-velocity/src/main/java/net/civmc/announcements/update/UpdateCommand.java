package net.civmc.announcements.update;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import java.time.Instant;
import net.civmc.announcements.AnnouncementsPlugin;
import net.civmc.announcements.BossBarManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class UpdateCommand implements SimpleCommand {

    private final ProxyServer server;
    private final AnnouncementsPlugin plugin;
    private final UpdateListener listener;
    private final BossBarManager bossBarManager;
    private final Component message;


    private volatile Runnable remove;

    public UpdateCommand(ProxyServer server, AnnouncementsPlugin plugin, UpdateListener listener, BossBarManager bossBarManager, Component message) {
        this.server = server;
        this.plugin = plugin;
        this.listener = listener;
        this.bossBarManager = bossBarManager;
        this.message = message;
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length > 0 && args[0].equalsIgnoreCase("cancel")) {
            if (remove != null) {
                remove.run();
            }
            listener.cancel();
            source.sendPlainMessage("Cancelled restart.");
            return;
        }

        if (args.length < 2) {
            source.sendPlainMessage("Usage: /" + invocation.alias() + " <timestamp seconds> <block players boolean>");
            return;
        }

        if (listener.isRestarting()) {
            source.sendPlainMessage("An update is already scheduled.");
            return;
        }

        long timestamp;
        try {
            timestamp = Long.parseLong(args[0]);
        } catch (NumberFormatException ex) {
            source.sendPlainMessage("Invalid timestamp");
            return;
        }


        Instant start = Instant.now();
        if (timestamp <= start.getEpochSecond()) {
            source.sendPlainMessage("Timestamp is in the past");
            return;
        }

        Instant end = Instant.ofEpochSecond(timestamp);

        boolean block = Boolean.parseBoolean(args[1]);

        listener.setRestart(end, block);
        BossBar bossBar = BossBar.bossBar(message, 1f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
        remove = bossBarManager.addBossBar(bossBar, message, end);
        source.sendPlainMessage("Scheduled update");
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("civannouncements.restart");
    }
}
