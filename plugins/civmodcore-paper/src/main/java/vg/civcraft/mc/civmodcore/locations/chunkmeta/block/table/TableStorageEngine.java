package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table;

import java.util.function.Consumer;
import java.util.logging.Logger;

import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.StorageEngine;

public abstract class TableStorageEngine <D extends TableBasedDataObject> implements StorageEngine {
	
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
	
	public abstract void insert(D data, ChunkCoord coord);
	
	public abstract void update(D data, ChunkCoord coord);
	
	public abstract void delete(D data, ChunkCoord coord);
	
	public abstract void fill(TableBasedBlockChunkMeta<D> chunkData, Consumer<D> insertFunction);

}
