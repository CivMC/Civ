package vg.civcraft.mc.citadel.activity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

class RegionData {
	private final Map<Integer, GroupData> data = new HashMap<>();
	private final AtomicBoolean isLoaded = new AtomicBoolean();
	private final Set<ChunkCoord> chunks = new HashSet<>();

	public void put(int groupId, GroupData groupData) {
		data.put(groupId, groupData);
	}

	public GroupData get(int groupId) {
		return data.computeIfAbsent(groupId, a -> new GroupData(null));
	}

	public void addChunk(ChunkCoord chunkCoord) {
		chunks.add(chunkCoord);
	}

	public boolean removeChunk(ChunkCoord chunkCoord) {
		chunks.remove(chunkCoord);
		return chunks.size() == 0;
	}

	public boolean isLoaded() {
		return isLoaded.get();
	}

	public void setLoaded() {
		isLoaded.set(true);
	}

	public Set<Integer> getGroups() {
		return data.keySet();
	}
}
