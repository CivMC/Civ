package vg.civcraft.mc.namelayer.misc;

import vg.civcraft.mc.mercury.MercuryAPI;
import vg.civcraft.mc.namelayer.NameLayerPlugin;

public class Mercury {

	/**
	 * Sends a message to all servers.
	 * @param message- The message to send.
	 */
	public static void message(String message){
		if (NameLayerPlugin.isMercuryEnabled()){
			MercuryAPI.sendGlobalMessage(message, "namelayer");
		}
	}
}
