package net.civmc.heliodor.vein;

import net.civmc.heliodor.vein.data.Vein;
import net.civmc.heliodor.vein.data.VeinPing;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.NumberConversions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class VeinCache {

    private final Plugin plugin;
    private final List<Vein> veins = new ArrayList<>();
    private final Map<Integer, Integer> additionalVeinBlocksMined = new HashMap<>();
    private final Map<Integer, Integer> veinOresMined = new HashMap<>();
    private final VeinDao dao;

    public VeinCache(Plugin plugin, VeinDao dao) {
        this.plugin = plugin;
        this.dao = dao;
    }

    public void load() {
        this.veins.addAll(dao.getVeins());

        Bukkit.getScheduler().runTaskTimer(plugin, this::save, 20 * 60, 20 * 60);
    }

    public void save() {
        List<Vein> veins = new ArrayList<>(this.veins);
        Map<Integer, Integer> additionalVeinBlocksMined = new HashMap<>(this.additionalVeinBlocksMined);
        Map<Integer, Integer> veinOresMined = new HashMap<>(this.veinOresMined);
        if (Bukkit.isStopping()) {
            updateVeins(veins, additionalVeinBlocksMined, veinOresMined);
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> updateVeins(veins, additionalVeinBlocksMined, veinOresMined));
        }
    }

    private void updateVeins(List<Vein> veins, Map<Integer, Integer> additionalVeinBlocksMined, Map<Integer, Integer> veinOresMined) {
        List<Vein> updatedVeins = new ArrayList<>();
        for (Vein vein : veins) {
            int id = vein.id();
            Integer blocksMined = additionalVeinBlocksMined.get(id);
            Integer oresMined = veinOresMined.get(id);
            if (blocksMined == null && oresMined == null) {
                continue;
            }

            try {
                if (dao.updateVein(id, blocksMined, oresMined, true)) {
                    updatedVeins.add(new Vein(
                        vein.id(),
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
                        vein.blocksAvailable() - (blocksMined == null ? 0 : blocksMined),
                        true,
                        vein.ores(),
                        vein.oresRemaining() - (oresMined == null ? 0 : oresMined)
                    ));
                }
            } catch (RuntimeException ex) {
                plugin.getLogger().log(Level.WARNING, "Updating vein", ex);
            }
        }

        if (Bukkit.isStopping()) {
            // It's not necessary to synchronize internal state anymore; we can terminate early
            return;
        }
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (Vein updatedVein : updatedVeins) {
                for (int i = 0; i < this.veins.size(); i++) {
                    Vein vein = this.veins.get(i);
                    if (vein.id() == updatedVein.id()) {
                        int blocksAvailableChange = vein.blocksAvailable() - updatedVein.blocksAvailable();
                        int oresChange = vein.oresRemaining() - updatedVein.oresRemaining();
                        this.additionalVeinBlocksMined.compute(vein.id(), (k, v) -> {
                            if (v != null) {
                                if (v <= blocksAvailableChange) {
                                    return null;
                                }
                                return v - blocksAvailableChange;
                            }
                            return null;
                        });
                        this.veinOresMined.compute(vein.id(), (k, v) -> {
                            if (v != null) {
                                if (v <= oresChange) {
                                    return null;
                                }
                                return v - oresChange;
                            }
                            return null;
                        });
                        this.veins.set(i, updatedVein);
                    }
                }
            }
        });
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
        VeinPing ping = null;
        for (Vein vein : this.veins) {
            if (!vein.world().equals(world) || !vein.type().equals(type)) {
                continue;
            }
            if (vein.oresRemaining() < vein.ores() * 0.5) {
                continue;
            }
            int vx = vein.x() + vein.offsetX();
            int vy = vein.y() + vein.offsetY();
            int vz = vein.z() + vein.offsetZ();
            if (NumberConversions.square(vx - x) + NumberConversions.square(vy - y) + NumberConversions.square(vz - z) <= NumberConversions.square(high + vein.radius())) {
                ping = VeinPing.HIGH;
            }
            if (NumberConversions.square(vx - x) + NumberConversions.square(vy - y) + NumberConversions.square(vz - z) <= NumberConversions.square(low + vein.radius())) {
                if (ping != VeinPing.HIGH) {
                    ping = VeinPing.LOW;
                }
            }
        }
        return ping;
    }

    public float getVeinOreProbability(Vein vein, int offset) {
        int id = vein.id();
        int available = vein.blocksAvailable() - additionalVeinBlocksMined.getOrDefault(id, 0);
        if (available <= 0) {
            return 0;
        }
        int availableOffset = available - offset;
        int ores = vein.oresRemaining() - veinOresMined.getOrDefault(id, 0);
        if (availableOffset <= 0) {
            return ores > 0 ? 1 : 0;
        }
        return ores / (float) available;
    }

    public void incrementBlocksMined(Vein vein, int amount) {
        additionalVeinBlocksMined.merge(vein.id(), amount, Integer::sum);
    }

    public void decrementOres(Vein vein) {
        veinOresMined.merge(vein.id(), 1, Integer::sum);
    }

    public List<Vein> getVeins() {
        return veins;
    }

    public boolean addVein(Vein vein) {
        int id = this.dao.addVein(vein);
        if (id != -1) {
            Bukkit.getScheduler().runTask(plugin, () -> this.veins.add(new Vein(
                id,
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
                vein.blocksAvailable(),
                vein.discovered(),
                vein.ores(),
                vein.oresRemaining()
            )));
            return true;
        } else {
            return false;
        }
    }
}
