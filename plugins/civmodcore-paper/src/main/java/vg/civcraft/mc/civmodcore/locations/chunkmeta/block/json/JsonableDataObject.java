package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.json;

import org.bukkit.Location;

import com.google.gson.JsonObject;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public abstract class JsonableDataObject extends BlockDataObject<JsonableDataObject> {

	protected static Location parseLocationFromJson(JsonObject json, JsonBasedBlockChunkMeta meta) {
		JsonObject inner = json.get(" ").getAsJsonObject();
		int x = inner.get("x").getAsInt();
		int y = inner.get("y").getAsInt();
		int z = inner.get("z").getAsInt();
		return new Location(meta.getWorld(), x, y, z);
	}

	public JsonableDataObject(Location location) {
		super(location);
	}

	public abstract void concreteSerialize(JsonObject base);

	protected JsonObject serialize() {
		JsonObject json = new JsonObject();
		JsonObject inner = new JsonObject();
		inner.addProperty("x", location.getBlockX());
		inner.addProperty("y", location.getBlockY());
		inner.addProperty("z", location.getBlockZ());
		json.add(" ", inner);
		concreteSerialize(json);
		return json;
	}

}
