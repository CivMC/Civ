package net.minelink.ctplus.worldguard.api;

import org.bukkit.Location;

public class WorldGuardHelperImpl implements WorldGuardHelper {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return true;
    }

}
