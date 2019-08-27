package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Supplier;

public class ChunkMetaFactory {

	private static ChunkMetaFactory instance;
	public static ChunkMetaFactory getInstance() {
		if (instance == null) {
			instance = new ChunkMetaFactory();
		}
		return instance;
	}
	private Map<String, Integer> pluginToInternalIdMapping;

	private Map<Integer, Supplier<ChunkMeta<?>>> metaInstanciators;

	private ChunkMetaFactory() {
		pluginToInternalIdMapping = new HashMap<>();
		metaInstanciators = new TreeMap<>();
	}

	Collection<Entry<Integer, Supplier<ChunkMeta<?>>>> getEmptyChunkFunctions() {
		return metaInstanciators.entrySet();
	}

	public void registerPlugin(String name, int id, Supplier<ChunkMeta<?>> generator) {
		metaInstanciators.put(id, generator);
		pluginToInternalIdMapping.put(name, id);
	}

}
