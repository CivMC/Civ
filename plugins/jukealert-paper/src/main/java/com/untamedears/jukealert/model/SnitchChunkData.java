package com.untamedears.jukealert.model;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class SnitchChunkData extends TableBasedBlockChunkMeta<Snitch> {

	public SnitchChunkData(boolean isNew, TableStorageEngine<Snitch> storage) {
		super(isNew, storage);
	}

}
