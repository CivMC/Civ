package vg.civcraft.mc.citadel;

import org.bukkit.Location;
import org.bukkit.block.Block;

import vg.civcraft.mc.citadel.model.CitadelChunkData;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class ReinforcementManager {

	private BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData;

	ReinforcementManager(
			BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData) {
		this.chunkMetaData = chunkMetaData;
	}

	public Reinforcement getReinforcement(Location location) {
		return (Reinforcement) chunkMetaData.get(location);
	}

	public Reinforcement getReinforcement(Block block) {
		return getReinforcement(block.getLocation());
	}

	public void putReinforcement(Reinforcement reinforcement) {
		chunkMetaData.put(reinforcement);
	}

	void shutDown() {
		chunkMetaData.disable();
	}
}
