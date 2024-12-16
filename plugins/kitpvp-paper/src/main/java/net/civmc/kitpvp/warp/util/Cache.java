package net.civmc.kitpvp.warp.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {
    private List<String> warpIndex;
    private Map<String, Warp> warps;
    private Map<Player, Warp> playerLocations = new HashMap<>();

    public Cache(List<String> index, Map<String, Warp> warpMap) {
        warpIndex = index;
        warps = warpMap;
    }

    public List<String> getWarpIndex() {
        return warpIndex;
    }

    public @Nullable Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public void addWarp(Warp warp) {
        warps.put(warp.name().toLowerCase(), warp);
        warpIndex.add(warp.name().toLowerCase());
    }

    public void deleteWarp(String name) {
        warps.remove(name.toLowerCase());
        warpIndex.remove(name.toLowerCase());
    }

    public void setPlayerLocation(Player player, Warp warp) {
        playerLocations.put(player, warp);
    }

    public Warp getPlayerLocation(Player player) {
        return playerLocations.get(player);
    }
}
