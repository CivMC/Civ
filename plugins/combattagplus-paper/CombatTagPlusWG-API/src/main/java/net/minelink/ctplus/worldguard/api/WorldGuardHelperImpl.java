package net.minelink.ctplus.worldguard.api;

import org.bukkit.Location;

public final class WorldGuardHelperImpl implements WorldGuardHelper {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return true;
    }

}
