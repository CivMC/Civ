package net.civmc.heliodor.heliodor.vein;

import java.util.List;
import java.util.Map;

public interface VeinDao {
    List<String> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns);
}
