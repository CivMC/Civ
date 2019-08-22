package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.json.simple.JSONObject;

import com.google.gson.JsonObject;

/**
 * Represents data for one specific chunk for one specific plugin. Subclasses
 * must also implement their own static deserialization method as shown in this
 * class
 * 
 *
 */
public abstract class ChunkMeta {

	public abstract JSONObject serialize();

	public static ChunkMeta deserialize(JsonObject json) {
		throw new IllegalAccessError();
	}

}
