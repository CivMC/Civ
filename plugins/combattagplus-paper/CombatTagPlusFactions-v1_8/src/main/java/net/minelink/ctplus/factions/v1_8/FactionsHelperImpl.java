package net.minelink.ctplus.factions.v1_8;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.Faction;
import com.massivecraft.factions.struct.FFlag;
import net.minelink.ctplus.factions.api.FactionsHelper;
import org.bukkit.Location;

public class FactionsHelperImpl implements FactionsHelper {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        FLocation flocation = new FLocation(location);
        Faction faction = Board.getFactionAt(flocation);
        return faction.getFlag(FFlag.PVP);
    }

}
