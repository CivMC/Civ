package net.civmc.announcements;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BossBarManager {
    private record ActiveBossBar(BossBar bar, Component text, Instant start, Instant end) {

    }
    private final List<ActiveBossBar> bossBars = new ArrayList<>();
    private final Lock bossBarLock = new ReentrantLock();

    private final ProxyServer server;
    private final AnnouncementsPlugin plugin;

    public BossBarManager(ProxyServer server, AnnouncementsPlugin plugin) {
        this.server = server;
        this.plugin = plugin;
    }

    public void init() {
        server.getScheduler().buildTask(plugin, this::tickBossBars).repeat(250, TimeUnit.MILLISECONDS).schedule();
    }

    public Runnable addBossBar(BossBar bar, Component text, Instant expiration) {
        ActiveBossBar active = new ActiveBossBar(bar, text, Instant.now(), expiration);
        bossBarLock.lock();
        try {
            bossBars.add(active);
            if (text != null) {
                bar.name(TimeComponent.replace(text, Duration.between(Instant.now(), expiration)));
            }
            server.showBossBar(bar);
        } finally {
            bossBarLock.unlock();
        }
        return () -> {
            bossBarLock.lock();
            try {
                for (Iterator<ActiveBossBar> iterator = bossBars.iterator(); iterator.hasNext(); ) {
                    ActiveBossBar bossBar = iterator.next();
                    if (bossBar == active) {
                        server.hideBossBar(bar);
                        iterator.remove();
                        return;
                    }
                }
            } finally {
                bossBarLock.unlock();
            }
        };
    }

    private void tickBossBars() {
        bossBarLock.lock();
        try {
            for (Iterator<ActiveBossBar> iterator = bossBars.iterator(); iterator.hasNext(); ) {
                ActiveBossBar bossBar = iterator.next();

                Instant now = Instant.now();
                if (now.isAfter(bossBar.end)) {
                    iterator.remove();
                    server.hideBossBar(bossBar.bar);
                    continue;
                }

                if (bossBar.text != null) {
                    bossBar.bar.name(TimeComponent.replace(bossBar.text, Duration.between(now, bossBar.end())));
                }

                long totalMillis = bossBar.start.until(bossBar.end, ChronoUnit.MILLIS);
                long progessMillis = bossBar.start.until(now, ChronoUnit.MILLIS);

                float progress = 1 - (progessMillis / (float) totalMillis);
                bossBar.bar.progress(progress);
            }
        } finally {
            bossBarLock.unlock();
        }
    }

    @Subscribe
    public void on(ServerPostConnectEvent event) {
        bossBarLock.lock();
        try {
            for (ActiveBossBar bossBar : bossBars) {
                event.getPlayer().showBossBar(bossBar.bar);
            }
        } finally {
            bossBarLock.unlock();
        }

    }
}
