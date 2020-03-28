package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table;

import java.util.function.Consumer;
import java.util.logging.Logger;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.XZWCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedStorageEngine;

public abstract class TableStorageEngine<D extends TableBasedDataObject>
		implements BlockBasedStorageEngine<TableBasedDataObject> {

	protected ManagedDatasource db;
	protected Logger logger;

	public TableStorageEngine(Logger logger, ManagedDatasource db) {
		this.logger = logger;
		this.db = db;
	}

	public abstract void registerMigrations();

	public boolean updateDatabase() {
		registerMigrations();
		return db.updateDatabase();
	}

	public abstract void insert(D data, XZWCoord coord);

	public abstract void update(D data, XZWCoord coord);

	public abstract void delete(D data, XZWCoord coord);

	public abstract void fill(TableBasedBlockChunkMeta<D> chunkData, Consumer<D> insertFunction);
	
	@SuppressWarnings("unchecked")
	@Override
	public void persist(TableBasedDataObject data, short worldID, short pluginID) {
		if (data.getCacheState() == CacheState.NORMAL) {
			return;
		}
		int chunkX = BlockBasedChunkMeta.modulo(data.getLocation().getBlockX());
		int chunkZ = BlockBasedChunkMeta.modulo(data.getLocation().getBlockZ());
		XZWCoord coord = new XZWCoord(chunkX, chunkZ, worldID);
		switch(data.getCacheState()) {
		case DELETED:
			delete((D) data, coord);
			break;
		case MODIFIED:
			update((D) data, coord);
			break;
		case NEW:
			insert((D) data, coord);
			break;		
		}
	}

}
