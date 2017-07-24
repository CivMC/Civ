/**
 * Created by Aleksey on 02.06.2017.
 */

package com.aleksey.castlegates.plugins.citadel;

import org.bukkit.Location;

public class NoCitadel implements ICitadel {
    public boolean useJukeAlert() { return false; }
    public String getGroupName() { return null; }
    public boolean canAccessDoors(Location location) { return true; }
}
