package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class APIView {
	
	protected short pluginID;
	protected JavaPlugin plugin;
	
	APIView(JavaPlugin plugin, short pluginID) {
		this.plugin = plugin;
		this.pluginID = pluginID;
	}
	
	public abstract void disable();

}
