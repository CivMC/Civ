package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;

public class AutoBlockChunkMeta<D extends SerializableDataObject<D>>
		extends BlockBasedChunkMeta<D, AutoStorageEngine<D>> {

	public AutoBlockChunkMeta(AutoStorageEngine<D> storage) {
		super(false, storage);
	}

	@Override
	public void delete() {
		// block based data does not delete entire chunks
		throw new IllegalStateException();
	}

	@Override
	public void insert() {
		iterateAll(d -> {
			switch (d.getCacheState()) {
			case DELETED:
				storage.deleteData(this.pluginID, d);
				break;
			case MODIFIED:
				storage.updateData(this.pluginID, d);
				break;
			case NEW:
				storage.insertData(this.pluginID, d);
				break;
			case NORMAL:
				return;
			default:
				return;
			}
			d.setCacheState(CacheState.NORMAL);
		});
	}

	@Override
	public void update() {
		insert();
	}

	@Override
	public void populate() {
		storage.loadDataForChunk(this.pluginID, this.chunkCoord, data -> {
			Location loc = data.getLocation();
			put(modulo(loc.getBlockX()), loc.getBlockY(), modulo(loc.getBlockZ()), data, false);
		});
	}

}
