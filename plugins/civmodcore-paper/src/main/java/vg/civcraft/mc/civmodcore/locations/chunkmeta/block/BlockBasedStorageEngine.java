package vg.civcraft.mc.civmodcore.locations.chunkmeta.block;

public interface BlockBasedStorageEngine<D extends BlockDataObject<D>> extends StorageEngine {
	
	D getForLocation(int x, int y, int z, short worldID, short pluginID);
	
	void persist(D data, short worldID, short pluginID);

}
