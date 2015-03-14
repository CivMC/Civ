package net.minelink.ctplus;

import net.minelink.ctplus.factions.api.FactionsHelper;
import org.bukkit.Location;

public final class FactionsManager {

    private final CombatTagPlus plugin;

    private final FactionsHelper helper;

    FactionsManager(CombatTagPlus plugin, FactionsHelper helper) {
        this.plugin = plugin;
        this.helper = helper;
    }

    public boolean isPvpEnabledAt(Location loc) {
        return helper.isPvpEnabledAt(loc);
    }

}
