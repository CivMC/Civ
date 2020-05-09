package vg.civcraft.mc.civmodcore.locations.global;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;

public class GlobalLocationTracker<T extends LocationTrackable> {

	private Map<Location, T> tracked;
	private GlobalTrackableDAO<T> dao;
	private Map<Location, T> deleted;

	public GlobalLocationTracker(GlobalTrackableDAO<T> dao) {
		this.tracked = new HashMap<>();
		this.dao = dao;
		this.deleted = new HashMap<>();
	}
	
	public synchronized void initFromDB() {
		dao.loadAll(this::put);
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
	}

	public synchronized T remove(Location loc) {
		T removed = tracked.remove(loc);
		if (removed != null && removed.getCacheState() != CacheState.NEW) {
			deleted.put(loc, removed);
		}
		return removed;
	}

	public synchronized T remove(T trackable) {
		return remove(trackable.getLocation());
	}
}
