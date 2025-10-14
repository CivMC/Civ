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
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.data.TemporaryNodeMergeStrategy;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.PermissionNode;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;

public class QueueListener {

    private final Map<Player, QueueRecord> players = new ConcurrentHashMap<>();

    private final CivProxyPlugin plugin;
    private final ProxyServer server;

    public QueueListener(CivProxyPlugin plugin, ProxyServer server) {
      this.plugin = plugin;
      this.server = server;
    }

    record QueueRecord(Instant instant, String server) {

    }

    public void start() {
        server.getEventManager().register(plugin, this);
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

        event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().orElse(Component.text("Disconnected"))));
    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {
        // If we are close to the cap, don't allow direct connections to the server and force them to go to the queue
        // This prevents players being able to snipe positions and bypass the queue

        if (event.getPreviousServer() != null) {
            return;
        }
        RegisteredServer mainServer = event.getOriginalServer();
        String name = mainServer.getServerInfo().getName();
        if (name.equals("pvp")) {
            return;
        }

        if (mainServer.getPlayersConnected().size() >= 145 && !event.getPlayer().hasPermission("joinbypass.use")) {
//            RegisteredServer mini = server.getServer("mini").orElse(null);
//            if (mini != null && mini.getPlayersConnected().size() < 110) {
//                event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.getServer("mini").get()));
//            } else {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.getServer("pvp").get()));
//            }

            players.put(event.getPlayer(), new QueueRecord(Instant.now(), name));
        }
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
}
