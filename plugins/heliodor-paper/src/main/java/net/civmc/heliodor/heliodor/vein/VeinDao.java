package net.civmc.heliodor.heliodor.vein;

import java.util.Map;

public interface VeinDao {
    Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns);

    void addVein(Vein vein);
}
