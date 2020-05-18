package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.json;

import org.bukkit.Location;

import com.google.gson.JsonObject;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public abstract class JsonableDataObject<D extends JsonableDataObject<D>> extends BlockDataObject<D> {
	
	public JsonableDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}

	public abstract void concreteSerialize(JsonObject base);

	protected JsonObject serialize() {
		JsonObject json = new JsonObject();
		concreteSerialize(json);
		return json;
	}

}
