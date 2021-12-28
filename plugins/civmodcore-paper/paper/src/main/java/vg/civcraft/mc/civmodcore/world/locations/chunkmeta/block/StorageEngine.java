package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block;

import java.util.Collection;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.XZWCoord;

public interface StorageEngine {
	
	/**
	 * Gets all chunks this engine has data for
	 * @return All chunks this engine holds data for
	 */
	Collection<XZWCoord> getAllDataChunks();
	
	/**
	 * @return Should all data of this engine always be kept in memory
	 */
	boolean stayLoaded();
	
}
