package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

public class ChunkMetaLoadStatus {
	public final ChunkMeta<?> meta;
	public final boolean isLoaded;

	public ChunkMetaLoadStatus(ChunkMeta<?> meta, boolean isLoaded) {
		this.meta = meta;
		this.isLoaded = isLoaded;
	}
}
