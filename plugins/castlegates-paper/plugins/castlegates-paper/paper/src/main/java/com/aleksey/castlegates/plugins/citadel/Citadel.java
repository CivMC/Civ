/**
 * Created by Aleksey on 02.06.2017.
 */

package com.aleksey.castlegates.plugins.citadel;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.model.Reinforcement;

import java.util.List;

public class Citadel implements ICitadel {
    private String _groupName;
    private boolean _hasAccess;
    private boolean _useJukeAlert;

    public Citadel(Reinforcement rein, boolean hasAccess, boolean useJukeAlert) {
        _groupName = rein != null ? rein.getGroup().getName() : null;
        _hasAccess = hasAccess;
        _useJukeAlert = useJukeAlert;
    }

    public boolean useJukeAlert() { return _useJukeAlert; }

    public String getGroupName() {
        return _groupName;
    }

    public boolean canAccessDoors(Location location) {
        if(!_hasAccess) return false;

        Reinforcement rein = vg.civcraft.mc.citadel.Citadel
                .getInstance()
                .getReinforcementManager()
                .getReinforcement(location);

        return rein == null
            ? _groupName == null
            : _groupName != null && _groupName.equalsIgnoreCase(rein.getGroup().getName());
    }
}