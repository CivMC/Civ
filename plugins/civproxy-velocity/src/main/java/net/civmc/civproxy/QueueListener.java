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

    private final Map<Player, QueueRecord> players = new ConcurrentHashMap<>();
    private final Map<Player, KickRecord> kickReasons = new ConcurrentHashMap<>();

    private final CivProxyPlugin plugin;
    private final ProxyServer server;
    private final ZorwethVelocityPlugin zorweth;

    public QueueListener(CivProxyPlugin plugin, ProxyServer server, ZorwethVelocityPlugin zorweth) {
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
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.getServer("pvp").get()));
        event.getServerKickReason().ifPresent(reason -> kickReasons.put(event.getPlayer(), new KickRecord(Instant.now(), reason)));

        players.put(event.getPlayer(), new QueueRecord(Instant.now(), name));
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

        KickRecord record = kickReasons.remove(event.getPlayer());
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

        if (event.getPreviousServer() != null) {
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

        if (queueTarget.getPlayersConnected().size() >= 145 && !event.getPlayer().hasPermission("joinbypass.use")) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.getServer("pvp").get()));

            players.put(event.getPlayer(), new QueueRecord(Instant.now(), queueTarget.getServerInfo().getName()));
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
        event.getPlayer().sendMessage(Component.text("You cannot queue for that server unless your rocket route allows it.", NamedTextColor.RED));
    }

    private void onPreQueueConnect(final PreConnectEvent event) {
        final String targetName = event.getTargetServer().getName();
        if (!this.zorweth.isProtectedServer(targetName) || event.getPlayer().getPlayer().hasPermission("zorweth.admin")) {
            return;
        }
        final String expectedServer = getExpectedServer(event.getPlayer().getPlayer());
        if (targetName.equals(expectedServer)) {
            return;
        }
        event.setCancelled(true);
        event.getPlayer().getPlayer().sendMessage(Component.text("Your rocket route changed. Please queue for "
            + (expectedServer == null ? "the correct server" : expectedServer) + ".", NamedTextColor.RED));
    }

    @Subscribe
    public void onConnect(ServerPostConnectEvent event) {
        // Add players coming from the main server to the queue

        QueueRecord record = players.remove(event.getPlayer());
        if (record != null && record.instant().isAfter(Instant.now().minusSeconds(60))) {
            QueueManager queueManager = AjQueueAPI.getInstance().getQueueManager();
            AdaptedPlayer player = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(event.getPlayer().getUniqueId());
            queueManager.addToQueue(player, record.server());
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
