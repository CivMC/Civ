package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import org.bukkit.Location;

import com.google.gson.JsonObject;

public abstract class BlockDataObject  {

	private BlockBasedChunkMeta<? extends BlockDataObject> owningCache;
	protected final Location location;
	
	public BlockDataObject(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location for BlockDataObject can not be null");
		}
		this.location = location;
	}
	
	public Location getLocation() {
		return location;
	}
	
	void setOwningCache(BlockBasedChunkMeta<? extends BlockDataObject> owningCache) {
		this.owningCache = owningCache;
	}
	
	public void setDirty(boolean dirty) {
		if (dirty) {
			owningCache.setDirty(true);
		}
	}
	
	protected BlockBasedChunkMeta<? extends BlockDataObject> getOwningCache() {
		return owningCache;
	}

	public static BlockDataObject deserialize(JsonObject json, ChunkMeta meta) {
		throw new IllegalAccessError();
	}

	public JsonObject serialize() {
		JsonObject json = new JsonObject();
		JsonObject inner = new JsonObject();
		inner.addProperty("x", location.getBlockX());
		inner.addProperty("y", location.getBlockY());
		inner.addProperty("z", location.getBlockZ());
		json.add(" ", inner);
		concreteSerialize(json);
		return json;
	}
	
	protected static Location parseLocationFromJson(JsonObject json, ChunkMeta meta) {
		JsonObject inner = json.get(" ").getAsJsonObject();
		int x = inner.get("x").getAsInt();
		int y = inner.get("y").getAsInt();
		int z = inner.get("z").getAsInt();
		return new Location(meta.getWorld(), x, y, z);
	}
	
	public abstract void concreteSerialize(JsonObject base);

}
