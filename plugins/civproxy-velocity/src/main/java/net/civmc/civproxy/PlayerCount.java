package net.civmc.civproxy;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class PlayerCount {

    private static final MinecraftChannelIdentifier PLAYER_COUNT_ID = MinecraftChannelIdentifier.create("civproxy", "player_count");

    private final CivProxyPlugin plugin;
    private final ProxyServer server;

    public PlayerCount(CivProxyPlugin plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    public void start() {
        server.getScheduler().buildTask(plugin, () -> {
            try {
                Map<String, Integer> count = new HashMap<>();
                for (RegisteredServer server : server.getAllServers()) {
                    count.put(server.getServerInfo().getName(), server.getPlayersConnected().size());
                }

                for (RegisteredServer server : server.getAllServers()) {
                    server.sendPluginMessage(PLAYER_COUNT_ID, out -> {
                        out.writeInt(count.size() - 1);
                        for (Map.Entry<String, Integer> entry : count.entrySet()) {
                            if (!entry.getKey().equals(server.getServerInfo().getName())) {
                                out.writeUTF(entry.getKey());
                                out.writeInt(entry.getValue());
                            }
                        }
                    });
                }
            } catch (RuntimeException ex) {
                plugin.getLogger().warn("Ticking player counts", ex);
            }
        }).delay(Duration.ZERO).repeat(Duration.ofSeconds(2)).schedule();
    }
}
