package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table;

import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public class TableBasedDataObject extends BlockDataObject<TableBasedDataObject> {

	public TableBasedDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}
	
	public void setDirty() {
		setCacheState(CacheState.MODIFIED);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setCacheState(CacheState newState) {
		CacheState oldState = this.state;
		super.setCacheState(newState);
		if (this.state != CacheState.NORMAL && this.state != oldState && getOwningCache() != null) {
			((TableBasedBlockChunkMeta<TableBasedDataObject>) getOwningCache()).reportChange(this);
		}
	}

}
