package net.minelink.ctplus.hook;

import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Coord;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.bukkit.towny.utils.CombatUtil;
import org.bukkit.Location;

public final class TownyHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location loc) {
        TownyWorld world;
        world = TownyUniverse.getInstance().getWorld(loc.getWorld().getName());

        TownBlock townBlock = null;
        try {
            townBlock = world.getTownBlock(Coord.parseCoord(loc));
        } catch (NotRegisteredException ignore) {

        }

        return !CombatUtil.preventPvP(world, townBlock);
    }

}
