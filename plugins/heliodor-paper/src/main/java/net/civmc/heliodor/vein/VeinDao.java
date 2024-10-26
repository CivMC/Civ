package net.civmc.heliodor.vein;

import net.civmc.heliodor.vein.data.Vein;
import java.util.List;
import java.util.Map;

public interface VeinDao {
    Map<String, Boolean> getSpawnableTypes(Map<String, Integer> spawnFrequencyMinutes, Map<String, Integer> maxSpawns);

    List<Vein> getVeins();

    int addVein(Vein vein);

    boolean updateVein(int veinId, Integer blocksMined, Integer oresMined, boolean discovered);
}
