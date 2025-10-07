package net.civmc.kitpvp.ranked;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface RankedDao {
    int getKit(UUID player);
    void setKit(UUID player, int kit);
    double getElo(UUID player);
    void updateElo(UUID player, UUID opponent, UUID winner);
    List<Rank> getTop(int n);

    Map<UUID, Double> getAll();

    record Rank(UUID player, double elo) {}
}
