package vg.civcraft.mc.civmodcore.locations.global;

import java.util.Collection;
import java.util.logging.Logger;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public abstract class GlobalTrackableDAO<T extends LocationTrackable> {
	
	protected ManagedDatasource db;
	protected Logger logger;

	public GlobalTrackableDAO(ManagedDatasource db, CivModCorePlugin plugin) {
		this.db = db;
		this.logger = plugin.getLogger();
	}
	
	public abstract void insert(T t);
	
	public abstract void delete(T t);
	
	public abstract void update(T t);
	
	public abstract Collection<T> loadAll();


}
