package vg.civcraft.mc.civmodcore.locations.global;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;

public class GlobalLocationTracker<T extends LocationTrackable> {

	private Map<Location, T> tracked;
	private GlobalTrackableDAO<T> dao;
	private Map<Location, T> deleted;
	private Map<XZWCoord, Map<Location, T>> perChunk;

	public GlobalLocationTracker(GlobalTrackableDAO<T> dao) {
		this.tracked = new HashMap<>();
		this.dao = dao;
		this.deleted = new HashMap<>();
		this.perChunk = new HashMap<>();
	}
	
	public synchronized void initFromDB() {
		dao.loadAll(this::put);
	}

	public void handleChunkLoad(Chunk chunk) {
		Map<Location, T> perChunkMap = perChunk.get(XZWCoord.fromChunk(chunk));
		if (perChunkMap != null) {
			for(Map.Entry<Location,T> entry : perChunkMap.entrySet()) {
				int x = BlockBasedChunkMeta.modulo(entry.getKey().getBlockX());
				int z = BlockBasedChunkMeta.modulo(entry.getKey().getBlockZ());
				Block block = chunk.getBlock(x, entry.getKey().getBlockY(), z);
				entry.getValue().onChunkLoad(block);
			}
		}
	}

	public void handleChunkUnload(Chunk chunk) {
		Map<Location, T> perChunkMap = perChunk.get(XZWCoord.fromChunk(chunk));
		if (perChunkMap != null) {
			for(Map.Entry<Location,T> entry : perChunkMap.entrySet()) {
				int x = BlockBasedChunkMeta.modulo(entry.getKey().getBlockX());
				int z = BlockBasedChunkMeta.modulo(entry.getKey().getBlockZ());
				Block block = chunk.getBlock(x, entry.getKey().getBlockY(), z);
				entry.getValue().onChunkUnload(block);
			}
		}
	}

	public synchronized void persist() {
		deleted.values().forEach(dao::delete);
		for (T t : tracked.values()) {
			switch (t.getCacheState()) {
			case DELETED:
				dao.delete(t);
				break;
			case MODIFIED:
				dao.update(t);
				break;
			case NEW:
				dao.insert(t);
				break;
			case NORMAL:
			default:
				break;
			}
			t.setCacheState(CacheState.NORMAL);
		}
	}

	public synchronized T get(Location loc) {
		return tracked.get(loc);
	}

	public synchronized void put(T trackable) {
		tracked.put(trackable.getLocation(), trackable);
		Map<Location, T> chunkSpecificData = perChunk.computeIfAbsent(XZWCoord.fromLocation(
				trackable.getLocation()), s -> new HashMap<>());
		chunkSpecificData.put(trackable.getLocation(), trackable);
	}

	public synchronized T remove(Location loc) {
		T removed = tracked.remove(loc);
		if (removed != null && removed.getCacheState() != CacheState.NEW) {
			Map<Location, T> chunkSpecificData = perChunk.computeIfAbsent(XZWCoord.fromLocation(
					loc), s -> new HashMap<>());
			if (removed != chunkSpecificData.remove(loc)) {
				CivModCorePlugin.getInstance().getLogger().severe("Data removed from per chunk tracking did "
						+ "not match data in global tracking");
			}
			deleted.put(loc, removed);
		}
		return removed;
	}

	public synchronized T remove(T trackable) {
		return remove(trackable.getLocation());
	}

}
