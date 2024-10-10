package dev.drekamor.warp.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Cache {
    private List<String> warpIndex = new ArrayList<>();
    private HashMap<String, Warp> warps = new HashMap<>();
    private HashMap<Player, Warp> playerLocations = new HashMap<>();

    public Cache (List<String> warpIndex, HashMap<String, Warp> warps) {
        this.warpIndex = warpIndex;
        this.warps = warps;
    }

    public List<String> getWarpIndex() {
        return warpIndex;
    }

    public @Nullable Warp getWarp(String name) {
        return warps.get(name);
    }

    public void addWarp(Warp warp) {
        warps.put(warp.name(), warp);
        warpIndex.add(warp.name());
    }

    public void deleteWarp(String name){
        warps.remove(name);
        warpIndex.remove(name);
    }

    public void setPlayerLocation(Player player, Warp warp) {
        playerLocations.put(player, warp);
    }

    public Warp getPlayerLocation(Player player) {
        return playerLocations.get(player);
    }
}
