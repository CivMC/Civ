package vg.civcraft.mc.civmodcore.world.locations.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;

public class GlobalLocationTracker<T extends LocationTrackable> {

	private Map<Location, T> tracked;
	private GlobalTrackableDAO<T> dao;
	private Map<Location, T> deleted;
	private Map<Location, T> modified;
	private Map<XZWCoord, Map<Location, T>> perChunk;

	public GlobalLocationTracker(GlobalTrackableDAO<T> dao) {
		this.tracked = new HashMap<>();
		this.dao = dao;
		this.deleted = new HashMap<>();
		this.modified = new HashMap<>();
		this.perChunk = new HashMap<>();
	}
	
	public synchronized void initFromDB() {
		dao.loadAll(this::putUnmodified);
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

	public void persist() {
		persistDeleted();
		persistModified();
	}

	private void persistDeleted() {
		List<T> list;

		synchronized (this.deleted) {
			if (this.deleted.isEmpty())
				return;

			list = new ArrayList<>();
			list.addAll(this.deleted.values());
			this.deleted.clear();
		}

		list.forEach(dao::delete);
	}

	private void persistModified() {
		List<T> list;

		synchronized (this.modified) {
			if (this.modified.isEmpty())
				return;

			list = new ArrayList<>();
			list.addAll(this.modified.values());
			this.modified.clear();
		}

		for (T t : list) {
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

	public void put(T trackable) {
		putUnmodified(trackable);
		setModified(trackable);
	}

	public void setModified(T trackable) {
		synchronized (this.modified) {
			this.modified.put(trackable.getLocation(), trackable);
		}
	}

	private synchronized void putUnmodified(T trackable) {
		trackable.setTracker(this);

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

			synchronized (this.deleted) {
				this.deleted.put(loc, removed);
			}
		}
		return removed;
	}

	public synchronized T remove(T trackable) {
		return remove(trackable.getLocation());
	}

}
