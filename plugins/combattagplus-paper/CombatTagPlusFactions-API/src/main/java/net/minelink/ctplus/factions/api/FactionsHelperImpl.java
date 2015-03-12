package net.minelink.ctplus.factions.api;

import org.bukkit.Location;

public class FactionsHelperImpl implements FactionsHelper {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        return true;
    }

}
