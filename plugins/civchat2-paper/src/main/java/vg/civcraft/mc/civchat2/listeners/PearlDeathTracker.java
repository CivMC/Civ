package vg.civcraft.mc.civchat2.listeners;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PearlDeathTracker {

    private final Set<UUID> recentlyPearled = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void addRecentlyPearled(UUID playerId) {
        recentlyPearled.add(playerId);
    }

    public boolean wasPearledOnDeath(UUID playerId) {
        return recentlyPearled.remove(playerId);
    }
}
