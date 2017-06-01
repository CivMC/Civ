/**
 * Created by Aleksey on 29.05.2017.
 */

package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.CastleGates;
import com.untamedears.JukeAlert.manager.SnitchManager;
import com.untamedears.JukeAlert.model.Snitch;
import org.bukkit.Location;

import java.util.Set;

public class JukeAlert implements IJukeAlert {
    private SnitchManager snitchManager;
    private Location location;
    private int groupId;
    private Integer jukeAlertGroupId;

    public JukeAlert(SnitchManager snitchManager, Location location, int groupId) {
        this.snitchManager = snitchManager;
        this.location = location;
        this.groupId = groupId;
    }

    public int getJukeAlertGroupId() {
        if(this.jukeAlertGroupId == null) {
            this.jukeAlertGroupId = -1;

            if(this.groupId > 0 && CastleGates.getConfigManager().getInteractWithSnitches()) {
                Set<Snitch> snitches = this.snitchManager.findSnitches(this.location.getWorld(), this.location);

                if (snitches.size() > 0) {
                    double distance = CastleGates.getCitadelManager().getMaxRedstoneDistance();

                    for (Snitch snitch : snitches) {
                        if (snitch.getGroup().getGroupId() == this.groupId
                                && snitch.shouldToggleLevers()
                                && snitch.getLoc().distance(this.location) <= distance
                                )
                        {
                            this.jukeAlertGroupId = this.groupId;
                            break;
                        }
                    }
                }
            }
        }

        return this.jukeAlertGroupId;
    }
}
