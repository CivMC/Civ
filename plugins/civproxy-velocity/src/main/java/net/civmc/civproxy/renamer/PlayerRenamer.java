package net.civmc.civproxy.renamer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import javax.sql.DataSource;
import net.civmc.civproxy.CivProxyPlugin;

public class PlayerRenamer {

    private final CivProxyPlugin plugin;
    private final ProxyServer server;

    private final AssociationList associations;

    public PlayerRenamer(CivProxyPlugin plugin, ProxyServer server, DataSource source) {
        this.plugin = plugin;
        this.server = server;
        this.associations = new AssociationList(plugin.getLogger(), source);
    }

    @Subscribe
    public void on(GameProfileRequestEvent requestEvent) {
        GameProfile profile = requestEvent.getGameProfile();
        associations.addPlayer(profile.getName(), profile.getId());

        String name = associations.getCurrentName(profile.getId());
        if (name == null) {
            associations.addPlayer(profile.getName(), profile.getId());
            name = associations.getCurrentName(profile.getId());
        }

        requestEvent.setGameProfile(requestEvent.getGameProfile().withName(name));
    }

    public void start() {
        associations.migrate();
        server.getEventManager().register(plugin, this);
        server.getCommandManager().register(server.getCommandManager().metaBuilder("changeplayername").aliases("nlcpn").plugin(plugin).build(),
            new ChangePlayerNameCommand(server, associations));
    }
}
