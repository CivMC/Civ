/**
 * Created by Aleksey on 02.06.2017.
 */

package com.aleksey.castlegates.plugins.citadel;

import org.bukkit.Location;

public interface ICitadel {
    boolean hasAccess();
    boolean useJukeAlert();
    Integer getGroupId();
    boolean canAccessDoors(Location location);
}
