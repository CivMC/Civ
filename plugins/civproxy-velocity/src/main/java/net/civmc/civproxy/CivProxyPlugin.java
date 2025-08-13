package net.civmc.civproxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.slf4j.Logger;
import us.ajg0702.queue.api.AjQueueAPI;
import us.ajg0702.queue.api.QueueManager;
import us.ajg0702.queue.api.players.AdaptedPlayer;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Plugin(id = "civproxy", name = "CivProxy", version = "1.0.0", authors = {"Okx"})
public class CivProxyPlugin {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public CivProxyPlugin(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(KickedFromServerEvent event) {
        // Prevent players from inadvertently going from the pvp server to the main server
        // Velocity doesn't seem to have a config option for this

        RegisteredServer prev = event.getServer();
        KickedFromServerEvent.ServerKickResult result = event.getResult();
        System.out.println("kick");
        System.out.println(result);
        if (prev.getServerInfo().getName().equals("pvp") && result instanceof KickedFromServerEvent.RedirectPlayer rd && rd.getServer().getServerInfo().getName().equals("main")) {
            event.setResult(KickedFromServerEvent.DisconnectPlayer.create(event.getServerKickReason().orElse(Component.text("Kicked"))));
        }
    }

    private final Map<Player, Instant> players = new ConcurrentHashMap<>();

    @Subscribe
    public void onToMain(KickedFromServerEvent event) {
        if (!event.getServer().getServerInfo().getName().equals("main")) {
            return;
        }
        if (event.getPlayer().getCurrentServer().isPresent()) {
            return;
        }
        String reason = event.getServerKickReason().map(s -> PlainTextComponentSerializer.plainText().serialize(s)).orElse("");
        if (reason.toLowerCase().contains("ban")) {
            return;
        }
        event.setResult(KickedFromServerEvent.RedirectPlayer.create(server.getServer("pvp").get()));

        players.put(event.getPlayer(), Instant.now());
    }

    @Subscribe
    public void onConnect(ServerPreConnectEvent event) {
        if (event.getPreviousServer() != null) {
            return;
        }
        RegisteredServer mainServer = event.getOriginalServer();
        if (!mainServer.getServerInfo().getName().equals("main")) {
            return;
        }

        if (mainServer.getPlayersConnected().size() >= 100) {
            event.setResult(ServerPreConnectEvent.ServerResult.allowed(server.getServer("pvp").get()));

            players.put(event.getPlayer(), Instant.now());
        }
    }

    @Subscribe
    public void onConnect(ServerPostConnectEvent event) {
        Instant timestamp = players.remove(event.getPlayer());
        if (timestamp != null && timestamp.isAfter(Instant.now().minusSeconds(60))) {
            QueueManager queueManager = AjQueueAPI.getInstance().getQueueManager();
            AdaptedPlayer player = AjQueueAPI.getInstance().getPlatformMethods().getPlayer(event.getPlayer().getUniqueId());
            queueManager.addToQueue(player, "main");
        }
    }
}
