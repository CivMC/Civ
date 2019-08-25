package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.bukkit.World;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class ChunkMetaFactory {

	private Map<String, Integer> pluginToInternalIdMapping;
	private Map<Integer, Class<? extends ChunkMeta>> pluginIdToDataClass;
	private Map<Integer, Method> pluginIdToDeserializeMethod;
	private JsonParser jsonParser;

	public ChunkMetaFactory() {
		pluginToInternalIdMapping = new HashMap<>();
		pluginIdToDataClass = new TreeMap<>();
		pluginIdToDeserializeMethod = new TreeMap<>();
		jsonParser = new JsonParser();
	}

	ChunkMeta deserialize(String rawJson, int pluginID, World world) {
		JsonElement json = jsonParser.parse(rawJson);
		Method method = pluginIdToDeserializeMethod.get(pluginID);
		if (method == null) {
			return null;
		}
		Object generated;
		try {
			generated = method.invoke(null, json);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			CivModCorePlugin.getInstance().severe("Failed to load chunk data", e);
			return null;
		}
		ChunkMeta meta = (ChunkMeta) generated;
		meta.setPluginID(pluginID);
		meta.setWorld(world);
		return meta;
	}

	boolean registerPlugin(String name, int id, Class<? extends ChunkMeta> chunkMetaClass) {
		Method deserialMethod;
		try {
			deserialMethod = chunkMetaClass.getMethod("deserialize", JsonObject.class);
		} catch (NoSuchMethodException | SecurityException e) {
			CivModCorePlugin.getInstance().getLogger().log(Level.SEVERE,
					"Failed to load deserialize method for plugin " + name, e);
			return false;
		}
		pluginIdToDataClass.put(id, chunkMetaClass);
		pluginIdToDeserializeMethod.put(id, deserialMethod);
		pluginToInternalIdMapping.put(name, id);
		return true;
	}

}
