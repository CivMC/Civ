package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.auto;

import com.google.gson.JsonObject;
import org.bukkit.Location;

public abstract class JsonableDataObject<D extends JsonableDataObject<D>> extends SerializableDataObject<D> {
	
	public JsonableDataObject(Location location, boolean isNew) {
		super(location, isNew);
	}

	public abstract void concreteSerialize(JsonObject base);

	@Override
	public String serialize() {
		JsonObject json = new JsonObject();
		concreteSerialize(json);
		return json.toString();
	}

}
