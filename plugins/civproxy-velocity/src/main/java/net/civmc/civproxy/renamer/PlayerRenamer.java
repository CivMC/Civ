package net.civmc.civproxy.renamer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import net.civmc.civproxy.CivProxyPlugin;
import net.civmc.nameApi.NameAPI;

public class PlayerRenamer {

    private final CivProxyPlugin plugin;
    private final ProxyServer server;

    private final NameAPI nameAPI;

    public PlayerRenamer(CivProxyPlugin plugin, ProxyServer server, HikariConfig nameAPIConfig) {
        this.plugin = plugin;
        this.server = server;
        this.nameAPI = new NameAPI(plugin.getLogger(), nameAPIConfig);
    }

    @Subscribe
    public void on(GameProfileRequestEvent requestEvent) {
        GameProfile profile = requestEvent.getGameProfile();
        nameAPI.addPlayer(profile.getName(), profile.getId());

        String name = nameAPI.getCurrentName(profile.getId());
        if (name == null) {
            // shouldn't the above call have added them?
            nameAPI.addPlayer(profile.getName(), profile.getId());
            name = nameAPI.getCurrentName(profile.getId());
        }

        requestEvent.setGameProfile(requestEvent.getGameProfile().withName(name));
    }

    public void start() {
        nameAPI.migrate();
        server.getEventManager().register(plugin, this);
        server.getCommandManager().register(server.getCommandManager().metaBuilder("changeplayername").aliases("nlcpn").plugin(plugin).build(),
            new ChangePlayerNameCommand(server, nameAPI));
    }
}
