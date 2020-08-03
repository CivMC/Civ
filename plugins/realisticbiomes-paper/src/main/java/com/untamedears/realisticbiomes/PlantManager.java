package com.untamedears.realisticbiomes;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.block.Block;

import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.RBChunkCache;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.api.BlockBasedChunkMetaView;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableBasedDataObject;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.table.TableStorageEngine;

public class PlantManager {

	private BlockBasedChunkMetaView<RBChunkCache, TableBasedDataObject, TableStorageEngine<Plant>> chunkMetaData;

	PlantManager(BlockBasedChunkMetaView<RBChunkCache, TableBasedDataObject, TableStorageEngine<Plant>> chunkMetaData) {
		this.chunkMetaData = chunkMetaData;
	}
	
	public void applyForAllInChunk(Location location, Consumer<Plant> toApply) {
		//need to wrap lambda due to generic inheritance complications
		chunkMetaData.getChunkMeta(location).iterateAll(s -> toApply.accept((Plant) s));
	}

	public void deletePlant(Plant plant) {
		chunkMetaData.remove(plant);
	}

	public Plant getPlant(Block block) {
		return getPlant(block.getLocation());
	}

	public Plant getPlant(Location location) {
		return (Plant) chunkMetaData.get(location);
	}

	public void putPlant(Plant plant) {
		chunkMetaData.put(plant);
	}

	void shutDown() {
		chunkMetaData.disable();
	}

}
