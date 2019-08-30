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

	@Override
	public void delete() {
		this.cacheState = CacheState.DELETED;
	}

	public CacheState getCacheState() {
		return cacheState;
	}
	
	public void setDirty() {
		setCacheState(CacheState.MODIFIED);
	}

	@Override
	public void setCacheState(CacheState state) {
		this.cacheState = this.cacheState.progress(state);
		if (state == CacheState.MODIFIED) {
			getOwningCache().setCacheState(state);
		}
	}

}
