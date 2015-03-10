package net.minelink.ctplus.worldguard.v5;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.minelink.ctplus.worldguard.api.WorldGuardHelper;
import org.bukkit.Location;

public final class WorldGuardHelperImpl implements WorldGuardHelper {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc).allows(DefaultFlag.PVP);
    }

}
