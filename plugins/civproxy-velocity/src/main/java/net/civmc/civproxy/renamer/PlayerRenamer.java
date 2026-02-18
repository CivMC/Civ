package net.civmc.civproxy.renamer;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.util.GameProfile;
import javax.sql.DataSource;
import net.civmc.civproxy.CivProxyPlugin;
import net.civmc.nameapi.NameAPI;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class PlayerRenamer {
    private final CivProxyPlugin plugin;
    private final ProxyServer server;
    private final NameAPI nameAPI;
    private Map<String, List<String>> displayNames;
    private final File storageFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public PlayerRenamer(CivProxyPlugin plugin, ProxyServer server, DataSource source) {
    this.plugin = plugin;
    this.server = server;
    this.nameAPI = new NameAPI(plugin.getLogger(), source);
    
    File civproxyDir = new File("/shared-data");
    civproxyDir.mkdirs();
    
    this.storageFile = new File(civproxyDir, "display_names.json");
    
    this.displayNames = loadDisplayNames();
}

public boolean removeDisplayName(UUID uuid, String nameToRemove) {
    String uuidString = uuid.toString();
    List<String> names = displayNames.get(uuidString);
    
    if (names == null || names.isEmpty()) {
        return false;
    }
    
    boolean removed = names.remove(nameToRemove);
    if (removed) {
        saveDisplayNames();
    }
    return removed;
}

    private Map<String, List<String>> loadDisplayNames() {
        if (!storageFile.exists()) {
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(storageFile)) {
            Map<String, List<String>> names = gson.fromJson(reader, new TypeToken<Map<String, List<String>>>(){}.getType());
            if (names == null) {
                return new HashMap<>();
            }
            return names;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    private void saveDisplayNames() {
        storageFile.getParentFile().mkdirs();
        try {
            FileWriter writer = new FileWriter(storageFile);
            String json = gson.toJson(displayNames);
            writer.write(json);
            writer.flush();
            writer.close();
            
            if (storageFile.exists()) {
            } else {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDisplayName(UUID uuid, String name) {
        
        displayNames.computeIfAbsent(uuid.toString(), k -> new ArrayList<>()).add(name);
        saveDisplayNames();
    }

    @Subscribe
    public void on(GameProfileRequestEvent requestEvent) {
        GameProfile profile = requestEvent.getGameProfile();
        nameAPI.addPlayer(profile.getName(), profile.getId());
        String name = nameAPI.getCurrentName(profile.getId());
        if (name == null) {
            throw new IllegalStateException("Unknown name for " + profile.getName());
        }
        requestEvent.setGameProfile(requestEvent.getGameProfile().withName(name));
    }

    public void start() {
        nameAPI.migrate();
        server.getEventManager().register(plugin, this);
        server.getCommandManager().register(server.getCommandManager().metaBuilder("changeplayername").aliases("nlcpn").plugin(plugin).build(),
            new ChangePlayerNameCommand(server, nameAPI, this));
        server.getCommandManager().register(server.getCommandManager().metaBuilder("removeplayername").aliases("nlrpn").plugin(plugin).build(),
            new RemoveDisplayNameCommand(server, nameAPI, this));
    }
}