package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Set;

import org.json.simple.JSONObject;

import com.google.gson.JsonObject;

/**
 * 
 * Four level cache holding abstract block tied data. This is similar to
 * Minecrafts in memory storage of blocks and meant to be used for y-local dense
 * concentration of data.
 * 
 * @author maxopoly
 *
 * @param <T> Data type held within this chunk
 */
public class BlockBasedChunkMeta<T extends DummyDataObject> extends ChunkMeta {

	private static final int CHUNK_HEIGHT = 256;
	private static final int L1_SECTION_COUNT = 16;
	private static final int L2_SECTION_COUNT = 16;
	private static final int L3_X_SECTION_COUNT = 16;
	private static final int L1_SECTION_LENGTH = CHUNK_HEIGHT / L1_SECTION_COUNT;
	private static final int L2_SECTION_LENGTH = L1_SECTION_LENGTH / L2_SECTION_COUNT;
	private static final int L3_X_SECTION_LENGTH = 16;
	private static final int L4_Z_SECTION_LENGTH = 16;

	// This has to be an array of the abstract super type and not the generic,
	// because java struggles with instanciating
	// generic arrays
	private DummyDataObject[][][][] data;

	public BlockBasedChunkMeta() {
		data = new DummyDataObject[L1_SECTION_COUNT][][][];
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
	private DummyDataObject[][][] getL2SubArray(int l1Offset, boolean create) {
		DummyDataObject[][][] subArray = data[l1Offset];
		if (create && subArray == null) {
			subArray = new DummyDataObject[L2_SECTION_COUNT][][];
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
	private DummyDataObject[][] getL3XSubArray(DummyDataObject[][][] l2Section, int l2Offset, boolean create) {
		DummyDataObject[][] subArray = l2Section[l2Offset];
		if (create && subArray == null) {
			subArray = new DummyDataObject[L3_X_SECTION_COUNT][];
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
	private DummyDataObject[] getL4ZSubArray(DummyDataObject[][] l3Section, int l3XOffset, boolean create) {
		DummyDataObject[] subArray = l3Section[l3XOffset];
		if (create && subArray == null) {
			subArray = new DummyDataObject[L4_Z_SECTION_LENGTH];
			l3Section[l3XOffset] = subArray;
		}
		return subArray;
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
		DummyDataObject[] l4ZSection = getL4ZSubArrayAbsolute(x, y, true);
		l4ZSection[z] = blockData;
	}

	private DummyDataObject[] getL4ZSubArrayAbsolute(int x, int y, boolean create) {
		int yOffsetL1 = y / L1_SECTION_COUNT;
		DummyDataObject[][][] l2Section = getL2SubArray(yOffsetL1, create);
		if (l2Section == null) {
			return null;
		}
		int yOffsetL2 = y % L2_SECTION_COUNT;
		DummyDataObject[][] l3XSection = getL3XSubArray(l2Section, yOffsetL2, create);
		if (l3XSection == null) {
			return null;
		}
		return getL4ZSubArray(l3XSection, x, create);
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
		DummyDataObject[] l4ZSection = getL4ZSubArrayAbsolute(x, y, false);
		if (l4ZSection == null) {
			return null;
		}
		return (T) l4ZSection[z];
	}

	@Override
	public JSONObject serialize() {
		JSONObject l1Array = new JSONObject();
		for (int i = 0; i < data.length; i++) {
			DummyDataObject[][][] l2Cache = data[i];
			if (l2Cache == null) {
				continue;
			}
			JSONObject l2Array = new JSONObject();
			for (int j = 0; j < l2Cache.length; j++) {
				DummyDataObject[][] l3Cache = l2Cache[j];
				if (l3Cache == null) {
					continue;
				}
				JSONObject l3Array = new JSONObject();
				for (int k = 0; k < l3Cache.length; k++) {
					DummyDataObject[] l4Cache = l3Cache[k];
					if (l4Cache == null) {
						continue;
					}
					JSONObject l4Array = new JSONObject();
					for (int l = 0; l < l4Cache.length; l++) {
						if (l4Cache[l] != null) {
							l4Array.put(l, l4Cache[l]);
						}
					}
					if (!l4Array.isEmpty()) {
						l3Array.put(k, l4Array);
					}

				}
				if (!l3Array.isEmpty()) {
					l2Array.put(j, l3Array);
				}
			}
			if (!l2Array.isEmpty()) {
				l1Array.put(i, l2Array);
			}
		}
		return l1Array;
	}
	
	public static <T extends DummyDataObject> BlockBasedChunkMeta<T> deserialize(JSONObject json) {
		BlockBasedChunkMeta<T> meta = new BlockBasedChunkMeta<>();
		for(Object keyO : json.keySet()) {
			int key = (int) keyO;
		}
		
		Set <Object> gg = json.keySet();
	}

}
