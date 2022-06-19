package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.*;
import java.util.function.Supplier;

public class ChunkMetaFactory {

	private static ChunkMetaFactory instance;

	public static ChunkMetaFactory getInstance() {
		if (instance == null)
			instance = new ChunkMetaFactory();

		return instance;
	}

	private final Map<String, Short> pluginToInternalIdMapping;
	private final List<ChunkMetaInitializer> initializers;

	private ChunkMetaFactory() {
		this.pluginToInternalIdMapping = new HashMap<>();
		this.initializers = new ArrayList<>();
	}

	Collection<ChunkMetaInitializer> getInitializers() {
		return initializers;
	}

	public void registerPlugin(String name, short id, Supplier<ChunkMeta<?>> generator) {
		ChunkMetaInitializer initializer = new ChunkMetaInitializer(id, generator);

		initializers.add(initializer);
		pluginToInternalIdMapping.put(name, id);
	}

}
