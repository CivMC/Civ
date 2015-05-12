package net.minelink.ctplus.worldguard.v5;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Location;

public final class WorldGuardHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        return WGBukkit.getRegionManager(loc.getWorld()).getApplicableRegions(loc).allows(DefaultFlag.PVP);
    }

}
