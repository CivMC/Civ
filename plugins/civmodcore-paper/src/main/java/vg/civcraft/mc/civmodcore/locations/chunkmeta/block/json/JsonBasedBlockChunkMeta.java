package vg.civcraft.mc.civmodcore.locations.chunkmeta.block.json;

import java.util.Map.Entry;
import java.util.function.BiFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkCoord;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockBasedChunkMeta;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.block.BlockDataObject;

public abstract class JsonBasedBlockChunkMeta extends BlockBasedChunkMeta<JsonableDataObject, JsonStorageEngine> {

	public JsonBasedBlockChunkMeta(boolean isNew, JsonStorageEngine storage) {
		super(isNew, storage);
	}

	@Override
	public void delete() {
		storage.deleteChunkData(pluginID, chunkCoord.getWorldID(), chunkCoord.getX(), chunkCoord.getZ());
	}

	protected abstract Class<? extends JsonableDataObject> getDataClass();

	protected abstract BiFunction<ChunkCoord, JsonObject, JsonableDataObject> getDataDeserializer();

	@Override
	public void insert() {
		storage.insertChunkData(pluginID, chunkCoord.getWorldID(), chunkCoord.getX(), chunkCoord.getZ(), serialize());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void populate() {
		JsonObject l1Object = storage.loadChunkData(pluginID, chunkCoord.getWorldID(), chunkCoord.getX(),
				chunkCoord.getZ());
		BiFunction<ChunkCoord, JsonObject, JsonableDataObject> dataDeserializer = getDataDeserializer();
		for (Entry<String, JsonElement> l1Entry : l1Object.entrySet()) {
			int l1Key = Integer.parseInt(l1Entry.getKey());
			data[l1Key] = new BlockDataObject[L2_SECTION_COUNT][][];
			JsonObject l2Object = l1Entry.getValue().getAsJsonObject();
			for (Entry<String, JsonElement> l2Entry : l2Object.entrySet()) {
				int l2Key = Integer.parseInt(l2Entry.getKey());
				data[l1Key][l2Key] = new BlockDataObject[L3_X_SECTION_COUNT][];
				JsonObject l3Object = l2Entry.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> l3Entry : l3Object.entrySet()) {
					int l3Key = Integer.parseInt(l3Entry.getKey());
					data[l1Key][l2Key][l3Key] = new BlockDataObject[L4_Z_SECTION_LENGTH];
					JsonObject l4Object = l3Entry.getValue().getAsJsonObject();
					for (Entry<String, JsonElement> l4Entry : l4Object.entrySet()) {
						int l4Key = Integer.parseInt(l4Entry.getKey());
						JsonableDataObject value = dataDeserializer.apply(chunkCoord, l4Entry.getValue().getAsJsonObject());
						value.setOwningCache(this);
						data[l1Key][l2Key][l3Key][l4Key] = value;
					}
				}
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public JsonObject serialize() {
		JsonObject l1Array = new JsonObject();
		for (int i = 0; i < data.length; i++) {
			BlockDataObject[][][] l2Cache = data[i];
			if (l2Cache == null) {
				continue;
			}
			JsonObject l2Array = new JsonObject();
			for (int j = 0; j < l2Cache.length; j++) {
				BlockDataObject[][] l3Cache = l2Cache[j];
				if (l3Cache == null) {
					continue;
				}
				JsonObject l3Array = new JsonObject();
				for (int k = 0; k < l3Cache.length; k++) {
					BlockDataObject[] l4Cache = l3Cache[k];
					if (l4Cache == null) {
						continue;
					}
					JsonObject l4Array = new JsonObject();
					for (int l = 0; l < l4Cache.length; l++) {
						if (l4Cache[l] != null) {
							JsonableDataObject value = (JsonableDataObject) l4Cache[l];
							l4Array.add(String.valueOf(l), value.serialize());
						}
					}
					if (l4Array.size() != 0) {
						l3Array.add(String.valueOf(k), l4Array);
					}
				}
				if (l3Array.size() != 0) {
					l2Array.add(String.valueOf(j), l3Array);
				}
			}
			if (l2Array.size() != 0) {
				l1Array.add(String.valueOf(i), l2Array);
			}
		}
		return l1Array;
	}

	@Override
	public void update() {
		storage.updateChunkData(pluginID, chunkCoord.getWorldID(), chunkCoord.getX(), chunkCoord.getZ(), serialize());

	}

}
