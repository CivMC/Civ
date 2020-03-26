package vg.civcraft.mc.citadel.model;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedBlockChunkMeta;

public class CitadelChunkData extends TableBasedBlockChunkMeta<Reinforcement> {

	public CitadelChunkData(boolean isNew, CitadelDAO storage) {
		super(isNew, storage);
	}

}
