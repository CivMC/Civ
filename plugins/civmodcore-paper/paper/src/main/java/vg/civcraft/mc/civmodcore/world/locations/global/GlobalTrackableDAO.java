package vg.civcraft.mc.civmodcore.world.locations.global;

import java.util.function.Consumer;
import java.util.logging.Logger;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public abstract class GlobalTrackableDAO<T extends LocationTrackable> {
	
	protected ManagedDatasource db;
	protected Logger logger;

	public GlobalTrackableDAO(Logger logger, ManagedDatasource db) {
		this.db = db;
		this.logger = logger;
	}
	
	public abstract void registerMigrations();

	public boolean updateDatabase() {
		registerMigrations();
		return db.updateDatabase();
	}
	
	protected short getWorldID(Location loc) {
		return CivModCorePlugin.getInstance().getWorldIdManager().getInternalWorldId(loc.getWorld());
	}
	
	public abstract void insert(T t);
	
	public abstract void delete(T t);
	
	public abstract void update(T t);
	
	public abstract void loadAll(Consumer<T> insertFunction);

}
