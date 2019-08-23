package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import com.google.gson.JsonObject;

public abstract class BlockDataObject {

	public static BlockDataObject deserialize(JsonObject json) {
		throw new IllegalAccessError();
	}

	public abstract JsonObject serialize();

}
