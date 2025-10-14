package vg.civcraft.mc.namelayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class NameAPI {

    private static GroupManager groupManager;
    private static final Map<UUID, String> uuidToPlayer = new HashMap<>();
    private static final Map<String, UUID> playerToUuid = new HashMap<>();

    public NameAPI(GroupManager man) {
        groupManager = man;
        Bukkit.getScheduler().runTaskAsynchronously(NameLayerPlugin.getInstance(), () -> {
            Map<UUID, String> uuidToPlayer = new HashMap<>();
            Map<String, UUID> playerToUuid = new HashMap<>();
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                playerToUuid.put(offlinePlayer.getName(), offlinePlayer.getUniqueId());
                uuidToPlayer.put(offlinePlayer.getUniqueId(), offlinePlayer.getName());
            }
            Bukkit.getScheduler().runTask(NameLayerPlugin.getInstance(), () -> {
                this.uuidToPlayer.putAll(uuidToPlayer);
                this.playerToUuid.putAll(playerToUuid);
            });
        });
    }

    public static void associate(String playerName, UUID uuid) {
        uuidToPlayer.put(uuid, playerName);
        playerToUuid.put(playerName, uuid);
    }

    /**
     * @return Returns an instance of the GroupManager.
     */
    public static GroupManager getGroupManager() {
        return groupManager;
    }

    public static UUID getUUID(String s) {
        return playerToUuid.get(s);
    }

    public static String getCurrentName(UUID uuid) {
        return uuidToPlayer.get(uuid);
    }
}
