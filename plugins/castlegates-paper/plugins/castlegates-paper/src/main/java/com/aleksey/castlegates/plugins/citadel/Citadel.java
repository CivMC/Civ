/**
 * Created by Aleksey on 02.06.2017.
 */

package com.aleksey.castlegates.plugins.citadel;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import vg.civcraft.mc.citadel.reinforcement.PlayerReinforcement;
import vg.civcraft.mc.citadel.reinforcement.Reinforcement;

import java.util.List;

public class Citadel implements ICitadel {
    private List<Player> players;
    private String groupName;
    private boolean hasAccess;
    private boolean useJukeAlert;

    public Citadel(List<Player> players, PlayerReinforcement playerRein, boolean hasAccess, boolean useJukeAlert) {
        this.players = players;
        this.groupName = playerRein != null ? playerRein.getGroup().getName() : null;
        this.hasAccess = hasAccess;
        this.useJukeAlert = useJukeAlert;
    }

    public boolean hasAccess() {
        return this.hasAccess;
    }

    public boolean useJukeAlert() { return this.useJukeAlert; }

    public String getGroupName() {
        return this.groupName;
    }

    public boolean canAccessDoors(Location location) {
        if(!this.hasAccess) return false;

        Reinforcement rein = vg.civcraft.mc.citadel.Citadel.getReinforcementManager().getReinforcement(location);

        return rein == null || !(rein instanceof PlayerReinforcement)
            ? this.groupName == null
            : this.groupName != null && this.groupName.equalsIgnoreCase(((PlayerReinforcement)rein).getGroup().getName());
    }
}