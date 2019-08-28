package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table;

import java.util.function.Consumer;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public abstract class TableBasedBlockChunkMeta<D extends TableBasedDataObject>
		extends BlockBasedChunkMeta<TableBasedDataObject, TableStorageEngine<D>> {

	public TableBasedBlockChunkMeta(boolean isNew, TableStorageEngine<D> storage) {
		super(isNew, storage);
	}

	@SuppressWarnings("rawtypes")
	private void iterateAll(Consumer<D> functionToApply) {
		for (int i = 0; i < data.length; i++) {
			BlockDataObject[][][] l2Cache = data[i];
			if (l2Cache == null) {
				continue;
			}
			for (int j = 0; j < l2Cache.length; j++) {
				BlockDataObject[][] l3Cache = l2Cache[j];
				if (l3Cache == null) {
					continue;
				}
				for (int k = 0; k < l3Cache.length; k++) {
					BlockDataObject[] l4Cache = l3Cache[k];
					if (l4Cache == null) {
						continue;
					}
					for (int l = 0; l < l4Cache.length; l++) {
						if (l4Cache[l] != null) {
							@SuppressWarnings("unchecked")
							D value = (D) l4Cache[l];
							functionToApply.accept(value);
						}
					}
				}
			}
		}
	}

	@Override
	public void insert() {
		iterateAll(d -> {
			switch (d.getCacheState()) {
			case NORMAL:
				return;
			case MODIFIED:
				storage.update(d, chunkCoord);
				return;
			case NEW:
				storage.insert(d, chunkCoord);
				return;
			case DELETED:
				storage.delete(d, chunkCoord);
			}
		});
	}

	@Override
	public void delete() {
		iterateAll(d -> {
			storage.delete(d, chunkCoord);
		});
	}

	@Override
	public void update() {
		insert();
	}

	@Override
	public void populate() {
		storage.fill(this, data -> {
			Location loc = data.getLocation();
			put(modulo(loc.getBlockX(), 16), loc.getBlockY(), modulo(loc.getBlockZ(), 16), data, false);
		});
	}

}
