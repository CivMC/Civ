package vg.civcraft.mc.civmodcore.locations.chunkmeta.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public final class ChunkMetaViewTracker {
	
	private static ChunkMetaViewTracker instance;
	public static ChunkMetaViewTracker getInstance() {
		if (instance == null) {
			instance = new ChunkMetaViewTracker();
		}
		return instance;
	}

	private final Map<Short, ChunkMetaView<?>> pluginIdToView;
	private final List<SingleBlockAPIView<?>> singleBlockViews;

	private ChunkMetaViewTracker() {
		this.pluginIdToView = new TreeMap<>();
		this.singleBlockViews = new ArrayList<>();
	}

	public void put(SingleBlockAPIView<?> view) {
		this.singleBlockViews.add(view);
	}

	public void applyToAllSingleBlockViews(Consumer<SingleBlockAPIView<?>> function) {
		singleBlockViews.forEach(function);
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
