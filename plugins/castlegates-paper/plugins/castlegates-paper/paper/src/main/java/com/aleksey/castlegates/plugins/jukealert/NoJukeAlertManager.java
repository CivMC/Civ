<<<<<<< HEAD:paper/src/main/java/com/aleksey/castlegates/plugins/jukealert/NoJukeAlertManager.java
package com.aleksey.castlegates.plugins.jukealert;

import com.aleksey.castlegates.plugins.citadel.ICitadel;
import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public boolean hasJukeAlertAccess(Location location, String groupName) { return false; }
}
=======
package com.aleksey.castlegates.plugins.jukealert;

import org.bukkit.Location;

public class NoJukeAlertManager implements IJukeAlertManager {
	public boolean hasJukeAlertAccess(Location location, String groupName) { return false; }
}
>>>>>>> 57e838af0c60c6113cb06cd01d640ca23fb9ed29:src/main/java/com/aleksey/castlegates/plugins/jukealert/NoJukeAlertManager.java
