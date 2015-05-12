package net.minelink.ctplus.factions.v2_7;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MFlag;
import com.massivecraft.massivecore.ps.PS;
import net.minelink.ctplus.hook.Hook;
import org.bukkit.Location;

public class FactionsHook implements Hook {

    @Override
    public boolean isPvpEnabledAt(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColl.get().getFactionAt(ps);
        return faction.getFlag(MFlag.ID_PVP);
    }

}
