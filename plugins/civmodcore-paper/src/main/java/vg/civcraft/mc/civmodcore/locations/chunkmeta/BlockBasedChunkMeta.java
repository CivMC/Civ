package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map.Entry;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

/**
 * 
 * Four level cache holding abstract block tied data. This is similar to
 * Minecrafts in memory storage of blocks and meant to be used for y-local dense
 * concentration of data.
 * 
 * You must create a subclass of this, which implement the static deserialize
 * method with only a single JsonObject as parameter and passes the correct
 * class object to the deserialize function of this class. If your subclass adds
 * additional data to this you must adjust the serialize and deserialize methods
 * as appropriate for these values to persist
 * 
 * Not thread-safe
 * 
 * DO NOT USE THIS WHEN THE ASSOCIATED CHUNK IS NOT LOADED
 * 
 * @author maxopoly
 *
 * @param <T> Data type held within this chunk
 */
public class BlockBasedChunkMeta<T extends BlockDataObject> extends ChunkMeta {

	private static final int CHUNK_HEIGHT = 256;
	private static final int L1_SECTION_COUNT = 16;
	private static final int L2_SECTION_COUNT = CHUNK_HEIGHT / L1_SECTION_COUNT;
	private static final int L3_X_SECTION_COUNT = 16;
	private static final int L4_Z_SECTION_LENGTH = 16;

	@SuppressWarnings("unchecked")
	public static <T extends BlockDataObject> BlockBasedChunkMeta<T> deserialize(JsonObject l1Object,
			Class<T> dataClass) {
		BlockBasedChunkMeta<T> meta = new BlockBasedChunkMeta<>(false);
		Method instanciationMethod;
		try {
			instanciationMethod = dataClass.getMethod("deserialize", JsonObject.class);
		} catch (NoSuchMethodException | SecurityException e1) {
			CivModCorePlugin.getInstance().warning(dataClass.getName() + " does have not a deserialize method", e1);
			return null;
		}
		for (Entry<String, JsonElement> l1Entry : l1Object.entrySet()) {
			int l1Key = Integer.parseInt(l1Entry.getKey());
			meta.data[l1Key] = new BlockDataObject[L2_SECTION_COUNT][][];
			JsonObject l2Object = l1Entry.getValue().getAsJsonObject();
			for (Entry<String, JsonElement> l2Entry : l2Object.entrySet()) {
				int l2Key = Integer.parseInt(l2Entry.getKey());
				meta.data[l1Key][l2Key] = new BlockDataObject[L3_X_SECTION_COUNT][];
				JsonObject l3Object = l2Entry.getValue().getAsJsonObject();
				for (Entry<String, JsonElement> l3Entry : l3Object.entrySet()) {
					int l3Key = Integer.parseInt(l3Entry.getKey());
					meta.data[l1Key][l2Key][l3Key] = new BlockDataObject[L4_Z_SECTION_LENGTH];
					JsonObject l4Object = l3Entry.getValue().getAsJsonObject();
					for (Entry<String, JsonElement> l4Entry : l4Object.entrySet()) {
						int l4Key = Integer.parseInt(l4Entry.getKey());
						T value;
						try {
							value = (T) instanciationMethod.invoke(null, l4Entry.getValue().getAsJsonObject());
						} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
							CivModCorePlugin.getInstance().warning("Failed to instanciate data", e);
							return null;
						}
						meta.data[l1Key][l2Key][l3Key][l4Key] = value;
					}
				}
			}
		}
		return meta;
	}

	// This has to be an array of the abstract super type and not the generic,
	// because java struggles with instanciating
	// generic arrays
	private BlockDataObject[][][][] data;

	public BlockBasedChunkMeta(boolean isNew) {
		super(isNew);
		data = new BlockDataObject[L1_SECTION_COUNT][][][];
	}

	/**
	 * Retrieves data from the cache
	 * 
	 * @param x Relative x offset in the chunk within [0,16), also the total
	 *          x-coordinate modulo 16
	 * @param y Y-Level of the block
	 * @param z Relative z offset in the chunk within [0,16), also the total
	 *          z-coordinate modulo 16
	 * @return Data retrieved for the given coordinates, possibly null
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y, int z) {
		BlockDataObject[] l4ZSection = getL4ZSubArrayAbsolute(x, y, false);
		if (l4ZSection == null) {
			return null;
		}
		return (T) l4ZSection[z];
	}

	/**
	 * Retrieves a level 2 cache based on the index of this second level cache in
	 * the top level cache. Top level indices are based on y-level
	 * 
	 * @param l1Offset Initial L1 offset in the top level data structure
	 * @param create   Should the level 2 cache be created if it does not exist yet
	 * @return Retrieved cache, may be null if none exists and creation was not
	 *         requested
	 */
	private BlockDataObject[][][] getL2SubArray(int l1Offset, boolean create) {
		BlockDataObject[][][] subArray = data[l1Offset];
		if (create && subArray == null) {
			subArray = new BlockDataObject[L2_SECTION_COUNT][][];
			data[l1Offset] = subArray;
		}
		return subArray;
	}

	/**
	 * Retrieves a level 3 cache based on the index of this third level cache in the
	 * second level cache it is contained in. Second level indices are also based on
	 * y-level
	 * 
	 * @param l2Offset L2 offset in the given L1 cache
	 * @param create   Should the level 3 cache be created if it does not exist yet
	 * @return Retrieved cache, may be null if none exists and creation was not
	 *         requested
	 */
	private BlockDataObject[][] getL3XSubArray(BlockDataObject[][][] l2Section, int l2Offset, boolean create) {
		BlockDataObject[][] subArray = l2Section[l2Offset];
		if (create && subArray == null) {
			subArray = new BlockDataObject[L3_X_SECTION_COUNT][];
			l2Section[l2Offset] = subArray;
		}
		return subArray;
	}

	/**
	 * Retrieves a level 4 cache based on the index of this fourth level cache in
	 * the third level cache it is contained in. Third level indices are the
	 * relative x offset within the chunk
	 * 
	 * @param l3Offset L3 offset in the given L2 cache
	 * @param create   Should the level 4 cache be created if it does not exist yet
	 * @return Retrieved cache, may be null if none exists and creation was not
	 *         requested
	 */
	private BlockDataObject[] getL4ZSubArray(BlockDataObject[][] l3Section, int l3XOffset, boolean create) {
		BlockDataObject[] subArray = l3Section[l3XOffset];
		if (create && subArray == null) {
			subArray = new BlockDataObject[L4_Z_SECTION_LENGTH];
			l3Section[l3XOffset] = subArray;
		}
		return subArray;
	}

	private BlockDataObject[] getL4ZSubArrayAbsolute(int x, int y, boolean create) {
		int yOffsetL1 = y / L1_SECTION_COUNT;
		BlockDataObject[][][] l2Section = getL2SubArray(yOffsetL1, create);
		if (l2Section == null) {
			return null;
		}
		int yOffsetL2 = y % L2_SECTION_COUNT;
		BlockDataObject[][] l3XSection = getL3XSubArray(l2Section, yOffsetL2, create);
		if (l3XSection == null) {
			return null;
		}
		return getL4ZSubArray(l3XSection, x, create);
	}

	@Override
	boolean isEmpty() {
		for (BlockDataObject[][][] l2 : data) {
			if (l2 == null) {
				continue;
			}
			for (BlockDataObject[][] l3 : l2) {
				if (l3 == null) {
					continue;
				}
				for (BlockDataObject[] l4 : l3) {
					if (l4 == null) {
						continue;
					}
					for (BlockDataObject element : l4) {
						if (element != null) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/**
	 * Inserts data into the cache, overwriting any existing one
	 * 
	 * @param x         Relative x offset in the chunk within [0,16), also the total
	 *                  x-coordinate modulo 16
	 * @param y         Y-Level of the block
	 * @param z         Relative z offset in the chunk within [0,16), also the total
	 *                  z-coordinate modulo 16
	 * @param blockData Data to insert
	 */
	public void put(int x, int y, int z, T blockData) {
		BlockDataObject[] l4ZSection = getL4ZSubArrayAbsolute(x, y, true);
		l4ZSection[z] = blockData;
	}

	@Override
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
							l4Array.add(String.valueOf(l), l4Cache[l].serialize());
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

}
