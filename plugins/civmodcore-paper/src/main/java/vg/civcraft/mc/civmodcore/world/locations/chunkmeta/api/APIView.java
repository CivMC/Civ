package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class APIView {
	
	protected final short pluginID;
	protected final JavaPlugin plugin;
	
	APIView(JavaPlugin plugin, short pluginID) {
		this.plugin = plugin;
		this.pluginID = pluginID;
	}
	
	public abstract void disable();

}
