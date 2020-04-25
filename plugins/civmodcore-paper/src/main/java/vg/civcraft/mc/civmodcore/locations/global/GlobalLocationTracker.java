package vg.civcraft.mc.civmodcore.locations.global;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;

public class GlobalLocationTracker<T extends LocationTrackable> {

	private Map<Location, T> tracked;
	private GlobalTrackableDAO<T> dao;

	public GlobalLocationTracker(GlobalTrackableDAO<T> dao) {
		this.tracked = new HashMap<>();
		this.dao = dao;
	}
	
	public void initFromDB() {
		dao.loadAll(this::put);
	}

	public void persist() {
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

	public T get(Location loc) {
		return tracked.get(loc);
	}

	public void put(T trackable) {
		tracked.put(trackable.getLocation(), trackable);
	}

	public T remove(Location loc) {
		return tracked.remove(loc);
	}

	public T remove(T trackable) {
		return remove(trackable.getLocation());
	}
}
