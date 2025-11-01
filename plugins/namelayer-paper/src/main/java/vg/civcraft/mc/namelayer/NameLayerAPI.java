package vg.civcraft.mc.namelayer;

import com.zaxxer.hikari.HikariConfig;
import java.util.UUID;
import net.civmc.nameApi.NameAPI;
import org.jetbrains.annotations.Nullable;

public class NameLayerAPI {

    private static GroupManager groupManager;
    private static NameAPI nameAPI;

    public NameLayerAPI(GroupManager man, HikariConfig nameAPIConfig) {
        groupManager = man;
        nameAPI = new NameAPI(NameLayerPlugin.getInstance().getSLF4JLogger(), nameAPIConfig);
    }

    /**
     * @return Returns an instance of the GroupManager.
     */
    public static GroupManager getGroupManager() {
        return groupManager;
    }

    public static @Nullable UUID getUUID(String s) {
        return nameAPI.getUUID(s);
    }

    public static @Nullable String getCurrentName(UUID uuid) {
        return nameAPI.getCurrentName(uuid);
    }
}
