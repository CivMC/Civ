package net.minelink.ctplus;

import net.minelink.ctplus.worldguard.api.WorldGuardHelper;
import org.bukkit.Location;

public final class WorldGuardManager {

    private final CombatTagPlus plugin;

    private final WorldGuardHelper helper;

    public WorldGuardManager(CombatTagPlus plugin, WorldGuardHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
    }

    public boolean isPvpEnabledAt(Location loc) {
        return helper != null && helper.isPvpEnabledAt(loc);
    }

}
