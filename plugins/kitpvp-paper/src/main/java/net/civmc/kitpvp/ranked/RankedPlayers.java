package net.civmc.kitpvp.ranked;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.civmc.kitpvp.KitPvpPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class RankedPlayers {

    private Map<UUID, Double> elo;
    private List<UUID> eloList = new ArrayList<>();

    public RankedPlayers(RankedDao dao) {
        this.elo = dao.getAll();
        this.eloList = calculateList(this.elo);
        Bukkit.getScheduler().runTaskTimerAsynchronously(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
            Map<UUID, Double> all = dao.getAll();
            List<UUID> list = calculateList(all);
            Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(KitPvpPlugin.class), () -> {
                this.elo = all;
                this.eloList = list;
            });
        }, 20 * 60 * 5, 20 * 60 * 5);
    }

    public void setElo(UUID player, double elo) {
        this.elo.put(player, elo);
        this.eloList = calculateList(this.elo);
    }

    private List<UUID> calculateList(Map<UUID, Double> all) {
        return all.entrySet().stream()
            .sorted(Comparator.<Map.Entry<UUID, Double>>comparingDouble(Map.Entry::getValue).reversed())
            .map(Map.Entry::getKey)
            .toList();
    }

    public double getElo(UUID player) {
        return elo.getOrDefault(player, 1000D);
    }

    public int getRank(UUID player) {
        for (int i = 0; i < eloList.size(); i++) {
            if (eloList.get(i).equals(player)) {
                return i;
            }
        }
        return -1;
    }
}
