package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table;

import org.bukkit.Location;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public class TableBasedDataObject extends BlockDataObject<TableBasedDataObject> {

	private CacheState cacheState;

	public TableBasedDataObject(Location location, boolean isNew) {
		super(location);
		this.cacheState = isNew ? CacheState.NEW : CacheState.NORMAL;
	}

	public CacheState getCacheState() {
		return cacheState;
	}
	
	public void setDirty() {
		setCacheState(CacheState.MODIFIED);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCacheState(CacheState state) {
		CacheState oldState = this.cacheState;
		this.cacheState = this.cacheState.progress(state);
		if (cacheState != CacheState.NORMAL && cacheState != oldState) {
			getOwningCache().setCacheState(CacheState.MODIFIED);
			((TableBasedBlockChunkMeta<TableBasedDataObject>) getOwningCache()).reportChange(this);
		}
	}

}
