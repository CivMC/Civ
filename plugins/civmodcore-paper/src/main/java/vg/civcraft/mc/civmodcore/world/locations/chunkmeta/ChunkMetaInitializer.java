package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.function.Supplier;

public class ChunkMetaInitializer {

	public final short pluginId;
	public final Supplier<ChunkMeta<?>> generator;

	public ChunkMetaInitializer(short pluginId, Supplier<ChunkMeta<?>> generator) {
		this.pluginId = pluginId;
		this.generator = generator;
	}
}
