package vg.civcraft.mc.namelayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import net.civmc.nameapi.NameAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

public class NameLayerAPI implements Listener {

    private static GroupManager groupManager;
    private static NameAPI nameAPI;

    private static final Map<UUID, String> toName = new HashMap<>();
    private static final Map<String, UUID> toUuid = new HashMap<>();

    private static long highestKnown = 0;

    public static void init(GroupManager man, DataSource source) {
        groupManager = man;
        nameAPI = new NameAPI(NameLayerPlugin.getInstance().getSLF4JLogger(), source);

        Bukkit.getScheduler().runTaskTimerAsynchronously(NameLayerPlugin.getInstance(), () -> {
            NameAPI.PlayerMappingInfo info = nameAPI.getAllPlayerInfo(highestKnown);
            Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> {
                if (info.highest() != highestKnown) {
                    NameLayerPlugin.log(Level.INFO, "Received " + info.nameMapping().size() + " entries, highest " + highestKnown + " -> " + info.highest());
                }
                highestKnown = info.highest();

                toName.putAll(info.uuidMapping());
                toUuid.putAll(info.nameMapping());
            });
        }, 0, 600);
    }

    /**
     * @return Returns an instance of the GroupManager.
     */
    public static GroupManager getGroupManager() {
        return groupManager;
    }

    public static @Nullable UUID getUUID(String s) {
        return toUuid.get(s);
    }

    public static @Nullable String getCurrentName(UUID uuid) {
        return toName.get(uuid);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        toUuid.put(player.getName(), player.getUniqueId());
        toName.put(player.getUniqueId(), player.getName());
    }
}
