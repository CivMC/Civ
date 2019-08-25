package vg.civcraft.mc.citadel.model;

import com.google.gson.JsonObject;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.BlockBasedChunkMeta;

public class CitadelChunkData extends BlockBasedChunkMeta<Reinforcement> {

	public CitadelChunkData(boolean isNew) {
		super(isNew);
	}
	
	public static CitadelChunkData deserialize(JsonObject json) {
		return (CitadelChunkData) BlockBasedChunkMeta.deserialize(new CitadelChunkData(false), json, Reinforcement.class);
	}

}
