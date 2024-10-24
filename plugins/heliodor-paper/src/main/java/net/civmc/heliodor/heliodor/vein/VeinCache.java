package net.civmc.heliodor.heliodor.vein;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;
import java.util.ArrayList;
import java.util.List;

public class VeinCache {

    private final Plugin plugin;
    private final List<Vein> veins;
    private final VeinDao dao;

    public VeinCache(Plugin plugin, VeinDao dao) {
        this.plugin = plugin;
        this.dao = dao;
        this.veins = new ArrayList<>(dao.getVeins());
    }

    public List<CachedVein> getVeinsInRadius(String world, int x, int y, int z) {
        List<CachedVein> inRadius = new ArrayList<>();
        List<Vein> veinList = this.veins;
        for (int i = 0; i < veinList.size(); i++) {
            Vein vein = veinList.get(i);
            if (!vein.world().equals(world)) {
                continue;
            }
            if (NumberConversions.square(vein.x() - x) + NumberConversions.square(vein.y() - y) + NumberConversions.square(vein.z() - z) <= NumberConversions.square(vein.radius())) {
                inRadius.add(new CachedVein(vein, i));
            }
        }
        return inRadius;
    }

    public boolean addVein(Vein vein) {
        if (this.dao.addVein(vein)) {
            Bukkit.getScheduler().runTask(plugin, () -> this.veins.add(vein));
            return true;
        } else {
            return false;
        }
    }

    public void incrementVeinMined(int index, boolean spawnedOre) {
        Vein vein = this.veins.get(index);
        this.veins.set(index, new Vein(
            vein.type(),
            vein.spawnedAt(),
            vein.world(),
            vein.radius(),
            vein.x(),
            vein.y(),
            vein.z(),
            vein.offsetX(),
            vein.offsetY(),
            vein.offsetZ(),
            vein.blocksAvailableEstimate(),
            vein.blocksMined() + 1,
            true,
            vein.ores() - (spawnedOre ? 1 : 0)
        ));
    }

    public record CachedVein(Vein vein, int index) {
    }
}
