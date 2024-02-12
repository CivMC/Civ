package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block;

import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkMeta;

public class BlockDataObjectLoadStatus<D extends BlockDataObject> {
	public final D data;
	public final boolean isLoaded;

	public BlockDataObjectLoadStatus(D data, boolean isLoaded) {
		this.data = data;
		this.isLoaded = isLoaded;
	}
}
