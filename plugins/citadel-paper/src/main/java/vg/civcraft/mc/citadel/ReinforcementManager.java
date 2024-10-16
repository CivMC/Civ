package vg.civcraft.mc.citadel;

import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.citadel.model.CitadelChunkData;
import vg.civcraft.mc.citadel.model.Reinforcement;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.table.TableStorageEngine;

public class ReinforcementManager {

	private BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData;

	ReinforcementManager(
			BlockBasedChunkMetaView<CitadelChunkData, TableBasedDataObject, TableStorageEngine<Reinforcement>> chunkMetaData) {
		this.chunkMetaData = chunkMetaData;
	}

	/**
	 * Gets the reinforcement at the given location if one exists
	 * 
	 * @param location Location to get reinforcement for
	 * @return Reinforcement at the location or null if no reinforcement exists
	 *         there
	 */
	public Reinforcement getReinforcement(Location location) {
		return (Reinforcement) chunkMetaData.get(location);
	}

	/**
	 * Gets the reinforcement for the given block if one exists
	 * 
	 * @param block Block to get reinforcement for
	 * @return Reinforcement for the block or null if no reinforcement exists there
	 */
	public Reinforcement getReinforcement(Block block) {
		return getReinforcement(block.getLocation());
	}

	/**
	 * Inserts the given reinforcement into the tracking. If a reinforcement already
	 * exists at the same location it will be replaced.
	 * 
	 * @param reinforcement Reinforcement to insert
	 */
	public void putReinforcement(Reinforcement reinforcement) {
		chunkMetaData.put(reinforcement);
	}

	void shutDown() {
		chunkMetaData.disable();
	}
}
