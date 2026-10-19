package net.civmc.civproxy.sessionlimit;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import net.civmc.civproxy.CivProxyPlugin;
import net.civmc.civproxy.QueueListener;
import net.civmc.civproxy.sessionlimit.SessionLimitPolicy.Action;
import net.civmc.civproxy.sessionlimit.SessionLimitPolicy.Decision;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.events.PreConnectEvent;
import us.ajg0702.queue.api.events.PreQueueEvent;
import us.ajg0702.queue.api.players.QueuePlayer;
import us.ajg0702.queue.api.queues.QueueServer;

/**
 * Moves players off the limited server after a long unbroken session, but only while other players are waiting in
 * the queue for it. Players get a boss bar countdown for the last part of their session.
 */
public final class SessionLimitManager {

    public static final String BYPASS_PERMISSION = "civproxy.sessionlimit.bypass";
    public static final String ADMIN_PERMISSION = "civproxy.sessionlimit.admin";

    private static final Duration PRIORITY_SEND_WINDOW = Duration.ofMinutes(1);
    private static final Duration PRIORITY_QUEUE_MEMORY = Duration.ofHours(2);
    private static final Duration KEEP_OFFLINE_STREAKS = Duration.ofHours(6);
    private static final Duration RED_BAR_UNDER = Duration.ofMinutes(10);

    private final CivProxyPlugin plugin;
    private final ProxyServer server;
    private final QueueListener queueListener;
    private final SessionLimitConfig config;
    private final SessionLimitPolicy policy;
    private final SessionLimitMessages messages;
    private final Function<String, UUID> offlinePlayerResolver;

    private final Map<UUID, Tracked> tracked = new ConcurrentHashMap<>();
    // Joined the queue for the limited server while holding the priority they got for leaving it
    private final Map<UUID, Instant> queuedWithLeavePriority = new ConcurrentHashMap<>();
    // Sent to the limited server by ajQueue after queueing with leave priority
    private final Map<UUID, Instant> sentWithLeavePriority = new ConcurrentHashMap<>();

    public SessionLimitManager(final CivProxyPlugin plugin, final ProxyServer server, final QueueListener queueListener,
                               final SessionLimitConfig config, final Function<String, UUID> offlinePlayerResolver) {
        this.plugin = plugin;
        this.server = server;
        this.queueListener = queueListener;
        this.config = config;
        this.policy = new SessionLimitPolicy(config.limit(), config.warning());
        this.messages = new SessionLimitMessages(config);
        this.offlinePlayerResolver = offlinePlayerResolver;
    }

    private static final class Tracked {

        private final PlayStreak streak;
        private @Nullable Action lastAction;
        private @Nullable Duration lastChatWarning;
        private boolean graceEndingWarned;
        private @Nullable BossBar bar;
        private boolean barShown;

        private Tracked(final PlayStreak streak) {
            this.streak = streak;
        }

        private void resetNotices() {
            this.lastAction = null;
            this.lastChatWarning = null;
            this.graceEndingWarned = false;
        }
    }

    public void start() {
        this.server.getEventManager().register(this.plugin, this);
        AjQueueAPI.getInstance().listen(PreQueueEvent.class, this::onPreQueue);
        AjQueueAPI.getInstance().listen(PreConnectEvent.class, this::onQueueSend);

        final CommandManager commands = this.server.getCommandManager();
        commands.register(commands.metaBuilder("sessiontime").plugin(this.plugin).build(),
            new SessionTimeCommand(this));
        commands.register(commands.metaBuilder("sessionlimit").plugin(this.plugin).build(),
            new SessionLimitCommand(this, this.server));

        this.server.getScheduler().buildTask(this.plugin, this::tick)
            .delay(Duration.ofSeconds(1)).repeat(Duration.ofSeconds(1)).schedule();
        this.server.getScheduler().buildTask(this.plugin, this::maintain)
            .delay(Duration.ofMinutes(1)).repeat(Duration.ofMinutes(1)).schedule();

        this.plugin.getLogger().info("Session limit {}on {}: {} limit, countdown for the last {}, {} break resets,"
                + " {} warning once a queue forms",
            this.config.dryRun() ? "in dry-run mode " : "enabled ", this.config.server(),
            DurationFormat.words(this.config.limit()), DurationFormat.words(this.config.warning()),
            DurationFormat.words(this.config.breakReset()), DurationFormat.words(this.config.queueGrace()));
    }

    public SessionLimitConfig config() {
        return this.config;
    }

    public Optional<PlayStreak.Snapshot> snapshot(final UUID playerId) {
        final Tracked entry = this.tracked.get(playerId);
        return entry == null ? Optional.empty() : Optional.of(entry.streak.snapshot(Instant.now()));
    }

    public boolean reset(final UUID playerId) {
        final Tracked entry = this.tracked.get(playerId);
        if (entry == null) {
            return false;
        }
        synchronized (entry) {
            entry.streak.reset(Instant.now());
            entry.resetNotices();
        }
        return true;
    }

    public void setPlayed(final UUID playerId, final Duration played) {
        final Tracked entry = this.tracked.computeIfAbsent(playerId, ignored -> new Tracked(new PlayStreak()));
        synchronized (entry) {
            entry.streak.setPlayed(Instant.now(), played);
            entry.resetNotices();
        }
    }

    public @Nullable UUID resolvePlayer(final String name) {
        final Optional<Player> online = this.server.getPlayer(name);
        if (online.isPresent()) {
            return online.get().getUniqueId();
        }
        return this.offlinePlayerResolver.apply(name);
    }

    public boolean isQueueWaiting() {
        final QueueServer queue = AjQueueAPI.getInstance().getQueueManager().findServer(this.config.server());
        if (queue == null) {
            return false;
        }
        for (final QueuePlayer queued : queue.getQueueHolder().getAllPlayers()) {
            // ajQueue keeps disconnected players' places for a while; only count people actually waiting
            if (queued.getPlayer() != null) {
                return true;
            }
        }
        return false;
    }

    @Subscribe
    public void onServerPostConnect(final ServerPostConnectEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();
        final Instant now = Instant.now();
        final boolean onLimitedServer = isOnLimitedServer(player);

        if (onLimitedServer) {
            final Tracked entry = this.tracked.computeIfAbsent(playerId, ignored -> new Tracked(new PlayStreak()));
            final boolean leavePriorityReturn = consumeLeavePriorityReturn(playerId, now);
            synchronized (entry) {
                if (entry.streak.enter(now, this.config.breakReset(), leavePriorityReturn)) {
                    entry.resetNotices();
                }
                entry.lastAction = null;
            }
            if (leavePriorityReturn) {
                this.plugin.getLogger().info("{} came back to {} through the queue with leave priority; keeping their"
                    + " session at {}", player.getUsername(), this.config.server(),
                    DurationFormat.shortPlayed(entry.streak.played(now)));
            }
            return;
        }

        final Tracked entry = this.tracked.get(playerId);
        if (entry == null) {
            return;
        }
        synchronized (entry) {
            entry.streak.leave(now);
            entry.lastAction = null;
            hideBar(player, entry);
        }
    }

    @Subscribe
    public void onDisconnect(final DisconnectEvent event) {
        final Tracked entry = this.tracked.get(event.getPlayer().getUniqueId());
        if (entry == null) {
            return;
        }
        synchronized (entry) {
            entry.streak.leave(Instant.now());
            entry.lastAction = null;
            hideBar(event.getPlayer(), entry);
            entry.bar = null;
        }
    }

    private void onPreQueue(final PreQueueEvent event) {
        if (!event.getTarget().getName().equals(this.config.server())) {
            return;
        }
        final UUID playerId = event.getPlayer().getUniqueId();
        if (event.getPlayer().hasPermission(QueueListener.leavePriorityPermission(this.config.server()))) {
            this.queuedWithLeavePriority.put(playerId, Instant.now());
        } else {
            this.queuedWithLeavePriority.remove(playerId);
        }
    }

    private void onQueueSend(final PreConnectEvent event) {
        if (!event.getTargetServer().getName().equals(this.config.server())) {
            return;
        }
        final UUID playerId = event.getPlayer().getUniqueId();
        if (this.queuedWithLeavePriority.containsKey(playerId)) {
            this.sentWithLeavePriority.put(playerId, Instant.now());
        }
    }

    private boolean consumeLeavePriorityReturn(final UUID playerId, final Instant now) {
        this.queuedWithLeavePriority.remove(playerId);
        final Instant sentAt = this.sentWithLeavePriority.remove(playerId);
        return sentAt != null && now.isBefore(sentAt.plus(PRIORITY_SEND_WINDOW));
    }

    private void tick() {
        try {
            final Instant now = Instant.now();
            final boolean queueWaiting = isQueueWaiting();
            for (final Map.Entry<UUID, Tracked> tracked : this.tracked.entrySet()) {
                final Tracked entry = tracked.getValue();
                if (!entry.streak.isOnServer()) {
                    continue;
                }
                final Optional<Player> player = this.server.getPlayer(tracked.getKey());
                if (player.isEmpty()) {
                    // The disconnect was missed somehow; stop their clock
                    synchronized (entry) {
                        entry.streak.leave(now);
                        entry.lastAction = null;
                        entry.bar = null;
                        entry.barShown = false;
                    }
                    continue;
                }
                if (!isOnLimitedServer(player.get())) {
                    // Mid-switch; the connect event will catch up
                    continue;
                }
                synchronized (entry) {
                    update(player.get(), entry, now, queueWaiting);
                }
            }
        } catch (final RuntimeException exception) {
            this.plugin.getLogger().warn("Ticking session limit", exception);
        }
    }

    private void update(final Player player, final Tracked entry, final Instant now, final boolean queueWaiting) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            hideBar(player, entry);
            entry.lastAction = null;
            return;
        }

        final Action previous = entry.lastAction;
        final Decision decision = this.policy.evaluate(entry.streak.played(now), entry.streak.graceEndsAt(), now,
            queueWaiting, previous == Action.COUNTDOWN);
        entry.lastAction = decision.action();

        switch (decision.action()) {
            case NONE -> hideBar(player, entry);
            case COUNTDOWN -> {
                final Duration remaining = decision.remaining();
                showBar(player, entry, this.messages.countdownBar(remaining), progress(remaining, this.config.warning()),
                    remaining.compareTo(RED_BAR_UNDER) <= 0 ? BossBar.Color.RED : BossBar.Color.YELLOW);
                sendCountdownWarning(player, entry, remaining);
            }
            case OVER_LIMIT -> {
                hideBar(player, entry);
            }
            case START_GRACE -> {
                entry.streak.startGrace(now.plus(this.config.queueGrace()));
                entry.graceEndingWarned = false;
                this.plugin.getLogger().info("{}{} is past the session limit on {} and players are queueing; moving them"
                        + " in {}", dryRunPrefix(), player.getUsername(), this.config.server(),
                    DurationFormat.words(this.config.queueGrace()));
                send(player, this.messages.graceStarted());
                showBar(player, entry, this.messages.graceBar(this.config.queueGrace()), 1, BossBar.Color.RED);
            }
            case GRACE -> {
                final Duration remaining = decision.remaining();
                showBar(player, entry, this.messages.graceBar(remaining), progress(remaining, this.config.queueGrace()),
                    BossBar.Color.RED);
                if (!entry.graceEndingWarned && remaining.compareTo(Duration.ofMinutes(1)) <= 0) {
                    entry.graceEndingWarned = true;
                    send(player, this.messages.graceEnding(remaining));
                }
            }
            case GRACE_LAPSED -> {
                entry.streak.clearGrace();
                hideBar(player, entry);
                send(player, this.messages.queueCleared());
                this.plugin.getLogger().info("{}The queue for {} cleared before {} had to move", dryRunPrefix(),
                    this.config.server(), player.getUsername());
            }
            case MOVE -> move(player, entry, now);
        }
    }

    private void move(final Player player, final Tracked entry, final Instant now) {
        final Duration played = entry.streak.played(now);
        entry.streak.reset(now);
        entry.resetNotices();
        hideBar(player, entry);
        if (this.config.dryRun()) {
            this.plugin.getLogger().info("[dry-run] Would move {} ({}) to the lobby after {} on {}; players are queueing",
                player.getUsername(), player.getUniqueId(), DurationFormat.shortPlayed(played), this.config.server());
            return;
        }
        this.plugin.getLogger().info("Moving {} ({}) to the lobby after {} on {} so queued players can join",
            player.getUsername(), player.getUniqueId(), DurationFormat.shortPlayed(played), this.config.server());
        this.queueListener.moveToLobbyAndQueue(player, this.config.server(), this.messages.moved(),
            this.messages.movedDisconnect());
    }

    private void sendCountdownWarning(final Player player, final Tracked entry, final Duration remaining) {
        // The smallest warning threshold at or above the time left; chat warnings are sorted largest first
        Duration threshold = null;
        for (final Duration warning : this.config.chatWarnings()) {
            if (remaining.compareTo(warning) <= 0) {
                threshold = warning;
            }
        }
        if (threshold == null || (entry.lastChatWarning != null && entry.lastChatWarning.compareTo(threshold) <= 0)) {
            return;
        }
        entry.lastChatWarning = threshold;
        send(player, this.messages.countdownWarning(remaining));
    }

    private void maintain() {
        try {
            final Instant now = Instant.now();
            this.tracked.entrySet().removeIf(entry -> entry.getValue().streak.isStale(now, KEEP_OFFLINE_STREAKS)
                && this.server.getPlayer(entry.getKey()).isEmpty());
            this.queuedWithLeavePriority.values().removeIf(at -> now.isAfter(at.plus(PRIORITY_QUEUE_MEMORY)));
            this.sentWithLeavePriority.values().removeIf(at -> now.isAfter(at.plus(PRIORITY_SEND_WINDOW)));
        } catch (final RuntimeException exception) {
            this.plugin.getLogger().warn("Maintaining session limit", exception);
        }
    }

    private boolean isOnLimitedServer(final Player player) {
        return player.getCurrentServer()
            .map(connection -> connection.getServerInfo().getName().equals(this.config.server()))
            .orElse(false);
    }

    private void showBar(final Player player, final Tracked entry, final Component name, final float progress,
                         final BossBar.Color color) {
        if (this.config.dryRun()) {
            return;
        }
        if (entry.bar == null) {
            entry.bar = BossBar.bossBar(name, progress, color, BossBar.Overlay.PROGRESS);
        } else {
            entry.bar.name(name);
            entry.bar.progress(progress);
            entry.bar.color(color);
        }
        if (!entry.barShown) {
            player.showBossBar(entry.bar);
            entry.barShown = true;
        }
    }

    private void hideBar(final Player player, final Tracked entry) {
        if (entry.bar != null && entry.barShown) {
            player.hideBossBar(entry.bar);
        }
        entry.barShown = false;
    }

    private void send(final Player player, final Component message) {
        if (!this.config.dryRun()) {
            player.sendMessage(message);
        }
    }

    private String dryRunPrefix() {
        return this.config.dryRun() ? "[dry-run] " : "";
    }

    private static float progress(final Duration remaining, final Duration total) {
        if (total.isZero()) {
            return 0;
        }
        return Math.clamp((float) remaining.toMillis() / total.toMillis(), 0F, 1F);
    }
}
