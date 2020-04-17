package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.Map;
import java.util.TreeMap;;

public final class ChunkMetaViewTracker {
	
	private static ChunkMetaViewTracker instance;
	public static ChunkMetaViewTracker getInstance() {
		if (instance == null) {
			instance = new ChunkMetaViewTracker();
		}
		return instance;
	}

	private Map<Short, ChunkMetaView<?>> pluginIdToView;

	private ChunkMetaViewTracker() {
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
