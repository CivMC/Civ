package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockDataObject;
import vg.civcraft.mc.civmodcore.world.locations.files.FileCacheManager;

public abstract class TableBasedBlockChunkMeta<D extends TableBasedDataObject>
		extends BlockBasedChunkMeta<TableBasedDataObject, TableStorageEngine<D>> {

	private final List<D> modifiedEntries;
	private final FileCacheManager<D> fileCacheManager;

	public TableBasedBlockChunkMeta(boolean isNew, TableStorageEngine<D> storage) {
		this(isNew, storage, null);
	}

	public TableBasedBlockChunkMeta(boolean isNew, TableStorageEngine<D> storage, FileCacheManager<D> fileCacheManager) {
		super(isNew, storage);
		this.modifiedEntries = new ArrayList<>();
		this.fileCacheManager = fileCacheManager;
	}

	public void reportChange(D data) {
		modifiedEntries.add(data);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void put(int x, int y, int z, TableBasedDataObject blockData, boolean isNew) {
		super.put(x, y, z, blockData, isNew);
		if (isNew) {
			modifiedEntries.add((D) blockData);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void remove(TableBasedDataObject blockData) {
		super.remove(blockData);
		blockData.setCacheState(CacheState.DELETED);
		//this may look weird, but is what happens if the data was NEW previously, never written to the
		//db and doesn't need to be deleted from there either
		if (blockData.getCacheState() != CacheState.NORMAL) {
			modifiedEntries.add((D) blockData);
		}
	}

	@Override
	protected TableBasedDataObject remove(int x, int y, int z) {
		TableBasedDataObject data = super.remove(x, y, z);
		if (data != null) {
			data.setCacheState(CacheState.DELETED);
		}
		return data;
	}

	@Override
	public void insert() {
		boolean hasChanges = false;

		for (D data : modifiedEntries) {
			switch (data.getCacheState()) {
			case NORMAL:
				continue;
			case MODIFIED:
				storage.update(data, chunkCoord);
				hasChanges = true;
				break;
			case NEW:
				storage.insert(data, chunkCoord);
				hasChanges = true;
				break;
			case DELETED:
				storage.delete(data, chunkCoord);
				hasChanges = true;
				break;
			}
			data.setCacheState(CacheState.NORMAL);
		}
		modifiedEntries.clear();

		if (hasChanges)
			invalidateFileCache();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void delete() {
		if (iterateAll(d -> {
			storage.delete((D)d, chunkCoord);
		})) {
			invalidateFileCache();
		}
	}

	@Override
	public void update() {
		insert();
	}

	private void invalidateFileCache() {
		if (this.fileCacheManager != null)
			this.fileCacheManager.invalidate(this.chunkCoord.getWorld(), this.chunkCoord.getX(), this.chunkCoord.getZ());
	}

	@Override
	public void populate() {
		if (!populateFromFileCache())
			populateFromDB();
	}

	private boolean populateFromFileCache() {
		if (this.fileCacheManager == null)
			return false;

		// Aleksey's Temporary: long startTime = System.currentTimeMillis();

		List<D> objects = this.fileCacheManager.load(this.chunkCoord.getWorld(), this.chunkCoord.getX(), this.chunkCoord.getZ());
		if (objects == null)
			return false;

		for (D o : objects) {
			Location loc = o.getLocation();
			put(modulo(loc.getBlockX()), loc.getBlockY(), modulo(loc.getBlockZ()), o, false);
		}

		this.storage.afterFill(objects);

		// Aleksey's Temporary: CivModCorePlugin.getInstance().getLogger().warning("populateFromFileCache() :: world = " + this.chunkCoord.getWorld().getName() + ", x = " + this.chunkCoord.getX() + ", z = " + this.chunkCoord.getZ() + ", time = " + (System.currentTimeMillis() - startTime));

		return true;
	}

	private void populateFromDB() {
		// Aleksey's Temporary: long startTime = System.currentTimeMillis();

		List<D> objects = new ArrayList<>();

		storage.fill(this, data -> {
			Location loc = data.getLocation();
			put(modulo(loc.getBlockX()), loc.getBlockY(), modulo(loc.getBlockZ()), data, false);
			objects.add(data);
		});

		this.storage.afterFill(objects);

		// Aleksey's Temporary: CivModCorePlugin.getInstance().getLogger().warning("populateFromDB() :: world = " + this.chunkCoord.getWorld().getName() + ", x = " + this.chunkCoord.getX() + ", z = " + this.chunkCoord.getZ()+ ", time = " + (System.currentTimeMillis() - startTime));
	}

	@Override
	public void handleChunkUnload() {
		if (this.fileCacheManager == null)
			return;

		List<D> objects = new ArrayList<>();
		iterateAll(d -> {
			objects.add((D)d);
		});

		this.fileCacheManager.unload(this.chunkCoord.getWorld(), this.chunkCoord.getX(), this.chunkCoord.getZ(), objects);
	}
}
