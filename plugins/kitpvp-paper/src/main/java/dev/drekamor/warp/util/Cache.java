package dev.drekamor.warp.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cache {
    private static List<String> warpIndex;
    private static Map<String, Warp> warps;
    private static Map<Player, Warp> playerLocations = new HashMap<>();

    public static void initialiseCache(List<String> index, Map<String, Warp> warpMap) {
        warpIndex = index;
        warps = warpMap;
    }

    public static  List<String> getWarpIndex() {
        return warpIndex;
    }

    public static @Nullable Warp getWarp(String name) {
        return warps.get(name);
    }

    public static void addWarp(Warp warp) {
        warps.put(warp.name(), warp);
        warpIndex.add(warp.name());
    }

    public static void deleteWarp(String name){
        warps.remove(name);
        warpIndex.remove(name);
    }

    public static void setPlayerLocation(Player player, Warp warp) {
        playerLocations.put(player, warp);
    }

    public static Warp getPlayerLocation(Player player) {
        return playerLocations.get(player);
    }
}
