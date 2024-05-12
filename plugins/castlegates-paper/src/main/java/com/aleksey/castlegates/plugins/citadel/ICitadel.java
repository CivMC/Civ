/**
 * Created by Aleksey on 02.06.2017.
 */

package com.aleksey.castlegates.plugins.citadel;

import org.bukkit.Location;

public interface ICitadel {
    boolean useJukeAlert();
    String getGroupName();
    boolean canAccessDoors(Location location);
}
