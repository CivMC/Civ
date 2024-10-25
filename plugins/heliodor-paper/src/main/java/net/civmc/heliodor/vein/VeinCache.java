package net.civmc.heliodor.vein;

import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.data.VeinPing;
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
        this.veins = new ArrayList<>();
    }

    public void load() {
        this.veins.addAll(dao.getVeins());
    }

    public List<Vein> getVeinsInRadius(String world, int x, int y, int z) {
        List<Vein> veinList = new ArrayList<>();
        for (Vein vein : this.veins) {
            if (!vein.world().equals(world)) {
                continue;
            }
            if (NumberConversions.square(vein.x() - x) + NumberConversions.square(vein.y() - y) + NumberConversions.square(vein.z() - z) <= NumberConversions.square(vein.radius())) {
                veinList.add(vein);
            }
        }
        return veinList;
    }

    public VeinPing getVeinPing(String world, String type, int low, int high, int x, int y, int z) {
        for (Vein vein : this.veins) {
            if (!vein.world().equals(world) || !vein.type().equals(type)) {
                continue;
            }
            int vx = vein.x() + vein.offsetX();
            int vy = vein.y() + vein.offsetY();
            int vz = vein.z() + vein.offsetZ();
            if (NumberConversions.square(vx - x) + NumberConversions.square(vy - y) + NumberConversions.square(vz - z) <= NumberConversions.square(high + vein.radius())) {
                return VeinPing.HIGH;
            }
            if (NumberConversions.square(vx - x) + NumberConversions.square(vz - z) <= NumberConversions.square(low + vein.radius())) {
                return VeinPing.LOW;
            }
        }
        return null;
    }

    public List<Vein> getVeins() {
        return veins;
    }

    public boolean addVein(Vein vein) {
        if (this.dao.addVein(vein)) {
            Bukkit.getScheduler().runTask(plugin, () -> this.veins.add(vein));
            return true;
        } else {
            return false;
        }
    }
}
