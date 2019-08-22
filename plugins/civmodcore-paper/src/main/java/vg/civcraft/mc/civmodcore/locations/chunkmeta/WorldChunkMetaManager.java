package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Map;

public class WorldChunkMetaManager {

	public void flushAll() {

	}

	/**
	 * Gets the meta data associated with a certain chunk based on its chunk coords
	 * 
	 * @param x X chunk coord
	 * @param z Z chunk coord
	 * @return Map using the plugin name as key and the data for that plugin for the
	 *         selected chunk as value. May be empty, but values may not be null
	 */
	public Map<String, ChunkMeta> getChunkMeta(int x, int z) {

	}

}
