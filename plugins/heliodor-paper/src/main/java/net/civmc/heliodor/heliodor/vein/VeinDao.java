package net.civmc.heliodor.heliodor.vein;

import java.util.List;
import java.util.Map;

public interface VeinDao {
    Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns);

    List<Vein> getVeins();

    boolean addVein(Vein vein);
}
