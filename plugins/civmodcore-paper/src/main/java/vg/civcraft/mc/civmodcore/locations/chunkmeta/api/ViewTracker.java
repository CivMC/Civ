package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.Map;
import java.util.TreeMap;;

public final class ViewTracker {
	
	private static ViewTracker instance;
	public static ViewTracker getInstance() {
		if (instance == null) {
			instance = new ViewTracker();
		}
		return instance;
	}

	private Map<Short, ChunkMetaView<?>> pluginIdToView;

	private ViewTracker() {
		pluginIdToView = new TreeMap<>();
	}
	
	public void put(ChunkMetaView<?> view, short pluginID) {
		this.pluginIdToView.put(pluginID, view);
	}
	
	public ChunkMetaView<?> get(short pluginID) {
		return pluginIdToView.get(pluginID);
	}
	
	public void remove(short id) {
		pluginIdToView.remove(id);
	}

}
