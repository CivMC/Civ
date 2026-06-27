package net.civmc.civproxy;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.civmc.zorweth.velocity.ZorwethVelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.events.PreConnectEvent;
import us.ajg0702.queue.api.events.PreQueueEvent;
import us.ajg0702.queue.api.players.AdaptedPlayer;

public class QueueListener {

    private final Map<UUID, QueueRecord> players = new ConcurrentHashMap<>();
    private final Map<UUID, KickRecord> kickReasons = new ConcurrentHashMap<>();
    private final Map<UUID, QueueRecord> queueConnects = new ConcurrentHashMap<>();

    private final CivProxyPlugin plugin;
    private final ProxyServer server;
    private final ZorwethVelocityPlugin zorweth;

    public QueueListener(final CivProxyPlugin plugin, final ProxyServer server, final ZorwethVelocityPlugin zorweth) {
        this.plugin = plugin;
        this.server = server;
        this.zorweth = zorweth;
    }

    record QueueRecord(Instant instant, String server) {

    }

    record KickRecord(Instant instant, Component reason) {

    }

    public void start() {
        server.getEventManager().register(plugin, this);
        AjQueueAPI.getInstance().listen(PreQueueEvent.class, this::onPreQueue);
        AjQueueAPI.getInstance().listen(PreConnectEvent.class, this::onPreQueueConnect);
    }

    @Subscribe
    public void onToMain(KickedFromServerEvent event) {
        // Other than banned players, kicked servers should go to the queue on the PvP server

        String name = event.getServer().getServerInfo().getName();
        if (name.equals("pvp")) {
            return;
        }
        if (event.getPlayer().getCurrentServer().isPresent()) {
            return;
        }
        final String queueTarget = getKickQueueTarget(event.getPlayer(), name);
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.getServer("pvp").get()));
        event.getServerKickReason().ifPresent(reason -> kickReasons.put(
            event.getPlayer().getUniqueId(), new KickRecord(Instant.now(), reason)));

        players.put(event.getPlayer().getUniqueId(), new QueueRecord(Instant.now(), queueTarget));
    }

    @Subscribe
    public void onFromPvP(KickedFromServerEvent event) {
        String name = event.getServer().getServerInfo().getName();
        if (!name.equals("pvp")) {
            return;
        }

        if (!(event.getResult() instanceof KickedFromServerEvent.RedirectPlayer)) {
            return;
        }

        KickRecord record = kickReasons.remove(event.getPlayer().getUniqueId());
        Component defaultReason;
        if (record != null && record.instant().isAfter(Instant.now().minusSeconds(60))) {
            defaultReason = record.reason();
        } else {
            defaultReason = Component.text("Disconnected");
        }
        event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().orElse(defaultReason)));
    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {
        // If we are close to the cap, don't allow direct connections to the server and force them to go to the queue
        // This prevents players being able to snipe positions and bypass the queue

        final RegisteredServer previousServer = event.getPreviousServer();
        if (previousServer != null && !previousServer.getServerInfo().getName().equals("pvp")) {
            return;
        }
        RegisteredServer requestedServer = event.getOriginalServer();
        String name = requestedServer.getServerInfo().getName();
        if (name.equals("pvp")) {
            return;
        }

        final RegisteredServer queueTarget;
        if (this.zorweth.isProtectedServer(name) && !this.zorweth.canBypass(event.getPlayer())) {
            final String expectedServer = getExpectedServer(event.getPlayer());
            if (expectedServer == null) {
                event.getPlayer().disconnect(Component.text("Unable to verify your rocket route. Please reconnect and try again.", NamedTextColor.RED));
                return;
            }
            queueTarget = this.server.getServer(expectedServer).orElse(null);
            if (queueTarget == null) {
                this.plugin.getLogger().error("Unable to find expected rocket route server {}", expectedServer);
                event.getPlayer().disconnect(Component.text("Unable to verify your rocket route. Please reconnect and try again.", NamedTextColor.RED));
                return;
            }
        } else {
            queueTarget = requestedServer;
        }

        final String queueTargetName = queueTarget.getServerInfo().getName();
        final boolean queueConnect = previousServer != null && isQueueConnect(event.getPlayer(), queueTargetName);
        if (queueTarget.getPlayersConnected().size() >= 145 && !event.getPlayer().hasPermission("joinbypass.use")) {
            if (previousServer != null) {
                if (queueConnect) {
                    this.plugin.getLogger().info("Allowing queued player {} from pvp to {}",
                        event.getPlayer().getUniqueId(), queueTargetName);
                    return;
                }
                final RegisteredServer pvpServer = server.getServer("pvp").get();
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(pvpServer));
                this.plugin.getLogger().info("Keeping player {} on pvp and adding them to {} queue because {} has {} players",
                    event.getPlayer().getUniqueId(), queueTargetName, queueTargetName, queueTarget.getPlayersConnected().size());
                addToQueue(event.getPlayer(), queueTargetName);
                return;
            }

            final RegisteredServer pvpServer = server.getServer("pvp").get();
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(pvpServer));
            this.plugin.getLogger().info("Redirecting player {} to pvp and adding them to {} queue because {} has {} players",
                event.getPlayer().getUniqueId(), queueTargetName, queueTargetName, queueTarget.getPlayersConnected().size());

            players.put(event.getPlayer().getUniqueId(), new QueueRecord(Instant.now(), queueTargetName));
        } else if (queueTarget != requestedServer) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(queueTarget));
        }
    }

    private void onPreQueue(final PreQueueEvent event) {
        if (!this.zorweth.isProtectedServer(event.getTarget().getName()) || event.getPlayer().hasPermission("zorweth.admin")) {
            return;
        }
        final String expectedServer = getExpectedServer(event.getPlayer());
        if (expectedServer != null && event.getTarget().getName().equals(expectedServer)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().sendMessage(Component.text("You are not eligible to join that server at this time.", NamedTextColor.RED));
    }

    private void onPreQueueConnect(final PreConnectEvent event) {
        final String targetName = event.getTargetServer().getName();
        if (!this.zorweth.isProtectedServer(targetName) || event.getPlayer().getPlayer().hasPermission("zorweth.admin")) {
            this.queueConnects.put(event.getPlayer().getUniqueId(), new QueueRecord(Instant.now(), targetName));
            this.plugin.getLogger().info("AjQueue is sending player {} to {}; allowing queue connection",
                event.getPlayer().getUniqueId(), targetName);
            return;
        }
        final String expectedServer = getExpectedServer(event.getPlayer().getPlayer());
        if (targetName.equals(expectedServer)) {
            this.queueConnects.put(event.getPlayer().getUniqueId(), new QueueRecord(Instant.now(), targetName));
            this.plugin.getLogger().info("AjQueue is sending player {} to expected server {}; allowing queue connection",
                event.getPlayer().getUniqueId(), targetName);
            return;
        }
        event.setCancelled(true);
        event.getPlayer().getPlayer().sendMessage(Component.text("Your rocket route changed. Please queue for "
            + (expectedServer == null ? "the correct server" : expectedServer) + ".", NamedTextColor.RED));
    }

    @Subscribe
    public void onConnect(ServerPostConnectEvent event) {
        // Add players coming from the main server to the queue

        QueueRecord record = players.remove(event.getPlayer().getUniqueId());
        if (record != null && record.instant().isAfter(Instant.now().minusSeconds(60))) {
            this.plugin.getLogger().info("Player {} reached pvp after redirect; adding them to {} queue",
                event.getPlayer().getUniqueId(), record.server());
            addToQueue(event.getPlayer(), record.server());
        }
    }

    @Subscribe
    public void onChangeFromMain(ServerPreConnectEvent event) {
        if (event.getPreviousServer() == null) {
            return;
        }
        String name = event.getPreviousServer().getServerInfo().getName();
        if (name.equals("pvp")) {
            return;
        }

        addPriority(event.getPlayer(), name);
    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        RegisteredServer server = event.getServer();
        String name = server.getServerInfo().getName();
        if (name.equals("pvp")
            || event.kickedDuringServerConnect()
            || event.getPlayer().getCurrentServer().map(c -> c.getServerInfo().getName().equals("pvp")).orElse(true)) {
            return;
        }

        addPriority(event.getPlayer(), name);
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        ServerConnection server = event.getPlayer().getCurrentServer().orElse(null);
        if (server == null) {
            return;
        }
        String name = server.getServerInfo().getName();
        if (name.equals("pvp")) {
            return;
        }

        addPriority(event.getPlayer(), name);
    }

    private void addPriority(Player player, String server) {
        // Players who just disconnected get 5 minutes of queue priority

        UserManager userManager = LuckPermsProvider.get().getUserManager();
        userManager.loadUser(player.getUniqueId()).thenAccept(user -> {
            if (user == null) {
                return;
            }
            user.data().add(
                PermissionNode.builder()
                    .permission("ajqueue.serverpriority." + server + ".10")
                    .expiry(5, TimeUnit.MINUTES)
                    .build(),
                TemporaryNodeMergeStrategy.REPLACE_EXISTING_IF_DURATION_LONGER);
            userManager.saveUser(user);
        });
    }

    private void addToQueue(final Player player, final String server) {
        final QueueManager queueManager = AjQueueAPI.getInstance().getQueueManager();
        final AdaptedPlayer adaptedPlayer = AjQueueAPI.getInstance().getPlatformMethods()
            .getPlayer(player.getUniqueId());
        if (adaptedPlayer == null) {
            this.plugin.getLogger().warn("Unable to add {} to {} queue: player was not known to AjQueue",
                player.getUniqueId(), server);
            return;
        }
        if (!queueManager.addToQueue(adaptedPlayer, server)) {
            this.plugin.getLogger().warn("AjQueue rejected adding {} to {} queue", player.getUniqueId(), server);
            return;
        }
        this.plugin.getLogger().info("Added player {} to {} queue", player.getUniqueId(), server);
    }

    private boolean isQueueConnect(final Player player, final String server) {
        final QueueRecord record = this.queueConnects.remove(player.getUniqueId());
        return record != null
            && record.instant().isAfter(Instant.now().minusSeconds(10))
            && record.server().equals(server);
    }

    private String getKickQueueTarget(final Player player, final String kickedServer) {
        if (!this.zorweth.isProtectedServer(kickedServer) || this.zorweth.canBypass(player)) {
            return kickedServer;
        }
        final String expectedServer = getExpectedServer(player);
        if (expectedServer == null) {
            return kickedServer;
        }
        if (!expectedServer.equals(kickedServer)) {
            this.plugin.getLogger().info("Player {} was kicked while connecting to {}, but their expected route is {}; queueing expected route",
                player.getUniqueId(), kickedServer, expectedServer);
        }
        return expectedServer;
    }

    private String getExpectedServer(final Player player) {
        try {
            return this.zorweth.getExpectedServer(player.getUniqueId());
        } catch (final SQLException exception) {
            this.plugin.getLogger().error("Failed to look up rocket route for " + player.getUniqueId(), exception);
            return null;
        }
    }

    private String getExpectedServer(final AdaptedPlayer player) {
        try {
            return this.zorweth.getExpectedServer(player.getUniqueId());
        } catch (final SQLException exception) {
            this.plugin.getLogger().error("Failed to look up rocket route for " + player.getUniqueId(), exception);
            return null;
        }
    }
}
