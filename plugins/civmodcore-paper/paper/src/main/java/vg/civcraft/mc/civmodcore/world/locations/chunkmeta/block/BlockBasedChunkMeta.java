package vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block;

import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.ChunkMeta;

/**
 * 
 * Four level cache holding abstract block tied data. This is similar to
 * Minecrafts in memory storage of blocks and meant to be used for y-local dense
 * concentration of data.
 * 
 * 
 * Not thread-safe
 * 
 * DO NOT USE THIS WHEN THE ASSOCIATED CHUNK IS NOT LOADED
 * 
 * @author maxopoly
 *
 * @param <D> Data type held within this chunk
 */
public abstract class BlockBasedChunkMeta<D extends BlockDataObject<D>, S extends StorageEngine> extends ChunkMeta<S> {

	protected static final int CHUNK_HEIGHT = 384;
	protected static final int L1_SECTION_COUNT = 24;
	protected static final int L2_SECTION_COUNT = CHUNK_HEIGHT / L1_SECTION_COUNT;
	protected static final int L3_X_SECTION_COUNT = 16;
	protected static final int L4_Z_SECTION_LENGTH = 16;

	// This has to be an array of the abstract super type and not the generic one,
	// because java struggles with instanciating
	// generic arrays
	protected BlockDataObject<D>[][][][] data;

	@SuppressWarnings("unchecked")
	public BlockBasedChunkMeta(boolean isNew, S storage) {
		super(isNew, storage);
		data = new BlockDataObject[L1_SECTION_COUNT][][][];
	}

	/**
	 * Retrieves data from the cache
	 * 
	 * @param block Block the data is tied to, may not be null
	 * @return Data for the given block, possibly null if no data exists for it
	 */
	public D get(Block block) {
		if (block == null) {
			throw new IllegalArgumentException("Block may not be null");
		}
		return get(block.getLocation());
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
	protected D get(int x, int y, int z) {
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(x, y, false);
		if (l4ZSection == null) {
			return null;
		}
		return (D) l4ZSection[z];
	}

	/**
	 * Retrieves data from the cache
	 * 
	 * @param location Location of the data, may not be null
	 * @return Data at the given location, possibly null if no data exists there
	 */
	public D get(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location may not be null");
		}
		return get(modulo(location.getBlockX()), location.getBlockY(), modulo(location.getBlockZ()));
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
	@SuppressWarnings("unchecked")
	private BlockDataObject<D>[][][] getL2SubArray(int l1Offset, boolean create) {
		BlockDataObject<D>[][][] subArray = data[l1Offset];
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
	@SuppressWarnings("rawtypes")
	private static BlockDataObject[][] getL3XSubArray(BlockDataObject[][][] l2Section, int l2Offset, boolean create) {
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
	 * @param l3Section L3 offset in the given L2 cache
	 * @param create   Should the level 4 cache be created if it does not exist yet
	 * @return Retrieved cache, may be null if none exists and creation was not
	 *         requested
	 */
	@SuppressWarnings("unchecked")
	private BlockDataObject<D>[] getL4ZSubArray(BlockDataObject<D>[][] l3Section, int l3XOffset, boolean create) {
		BlockDataObject<D>[] subArray = l3Section[l3XOffset];
		if (create && subArray == null) {
			subArray = new BlockDataObject[L4_Z_SECTION_LENGTH];
			l3Section[l3XOffset] = subArray;
		}
		return subArray;
	}

	@SuppressWarnings("unchecked")
	private BlockDataObject<D>[] getL4ZSubArrayAbsolute(int x, int y, boolean create) {
		int yOffsetL1 = (y + 64) / L1_SECTION_COUNT;
		BlockDataObject<D>[][][] l2Section = getL2SubArray(yOffsetL1, create);
		if (l2Section == null) {
			return null;
		}
		int yOffsetL2 = (y + 64) % L2_SECTION_COUNT;
		BlockDataObject<D>[][] l3XSection = getL3XSubArray(l2Section, yOffsetL2, create);
		if (l3XSection == null) {
			return null;
		}
		return getL4ZSubArray(l3XSection, x, create);
	}

	@Override
	public boolean isEmpty() {
		for (BlockDataObject<D>[][][] l2 : data) {
			if (l2 == null) {
				continue;
			}
			for (BlockDataObject<D>[][] l3 : l2) {
				if (l3 == null) {
					continue;
				}
				for (BlockDataObject<D>[] l4 : l3) {
					if (l4 == null) {
						continue;
					}
					for (BlockDataObject<D> element : l4) {
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
	 * Inserts data for the given block into the cache
	 * 
	 * @param block     Block to insert data for, may not be null
	 * @param blockData Data to insert
	 */
	public final void put(Block block, D blockData) {
		if (block == null) {
			throw new IllegalArgumentException("Block may not be null");
		}
		put(modulo(block.getX()), block.getY(), modulo(block.getZ()), blockData, true);
	}

	/**
	 * Inserts data into the cache, overwriting any existing one
	 * 
	 * @param x         Relative x offset in the chunk within [0,16), also the total
	 *                  x-coordinate modulo 16
	 * @param y         Y-Level of the block
	 * @param z         Relative z offset in the chunk within [0,16), also the total
	 *                  z-coordinate modulo 16
	 * @param blockData Data to insert, not null
	 */
	public void put(int x, int y, int z, D blockData, boolean isNew) {
		put(x, y, z, blockData, isNew, true);
	}

	/**
	 * Inserts data into the cache, overwriting any existing one
	 * 
	 * @param x                 Relative x offset in the chunk within [0,16), also
	 *                          the total x-coordinate modulo 16
	 * @param y                 Y-Level of the block
	 * @param z                 Relative z offset in the chunk within [0,16), also
	 *                          the total z-coordinate modulo 16
	 * @param blockData         Data to insert, not null
	 * @param deletePreexisting Should a preexisting entry at the location
	 *                          explicitly be removed by calling the appropriate
	 *                          method or just silently overwritten
	 */
	public void put(int x, int y, int z, D blockData, boolean isNew, boolean deletePreexisting) {
		if (blockData == null) {
			throw new IllegalArgumentException("Data may not be null");
		}
		if (isNew) {
			setCacheState(CacheState.MODIFIED);
		}
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(x, y, true);
		if (deletePreexisting && l4ZSection[z] != null) {
			remove(x, y, z);
		}
		blockData.setOwningCache(this);
		l4ZSection[z] = blockData;
	}

	/**
	 * * Inserts data at the given location into the cache
	 * 
	 * @param location  Location to insert data at
	 * @param blockData Data to insert
	 */
	public final void put(Location location, D blockData) {
		put(modulo(location.getBlockX()), location.getBlockY(), modulo(location.getBlockZ()), blockData, true);
	}

	/**
	 * Removes the entry at the given block if one exists and returns it
	 * 
	 * @param block Block to remove data from, may not be null
	 */
	public final D remove(Block block) {
		if (block == null) {
			throw new IllegalArgumentException("Block to remove can not be null");
		}
		return remove(block.getLocation());
	}

	/**
	 * Removes the given data from this cache. Will throw an IAE if the data is not
	 * in the cache
	 * 
	 * @param blockData Data to remove
	 */
	public void remove(D blockData) {
		if (blockData == null) {
			throw new IllegalArgumentException("Can not remove null from the cache");
		}
		Location loc = blockData.getLocation();
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(modulo(loc.getBlockX()), loc.getBlockY(), false);
		if (l4ZSection == null) {
			throw new IllegalArgumentException("Can not remove block data from cache, it is already gone");
		}
		if (l4ZSection[modulo(loc.getBlockZ())] != blockData) {
			throw new IllegalArgumentException("Can not remove block data from cache, it is already gone");
		}
		l4ZSection[modulo(loc.getBlockZ())] = null;
		setCacheState(CacheState.MODIFIED);
	}

	/**
	 * Removes the entry at the given location if one exists and returns it
	 * 
	 * @param x X-Coord of the entry to remove
	 * @param y Y-Coord of the entry to remove
	 * @param z Z-Coord of the entry to remove
	 * 
	 * @return Removed data
	 */
	protected D remove(int x, int y, int z) {
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(x, y, false);
		if (l4ZSection == null) {
			return null;
		}
		@SuppressWarnings("unchecked")
		D oldData = (D) l4ZSection[z];
		if (oldData != null) {
			l4ZSection[z] = null;
			setCacheState(CacheState.MODIFIED);
		}
		return oldData;
	}

	/**
	 * Removes the entry at the given location if one exists and returns it
	 * 
	 * @param location Location to remove data from, may not be null
	 */
	public final D remove(Location location) {
		if (location == null) {
			throw new IllegalArgumentException("Location to remove can not be null");
		}
		return remove(modulo(location.getBlockX()), location.getBlockY(), modulo(location.getBlockZ()));
	}

	@SuppressWarnings("rawtypes")
	public void iterateAll(Consumer<D> functionToApply) {
		for (int i = 0; i < data.length; i++) {
			BlockDataObject[][][] l2Cache = data[i];
			if (l2Cache == null) {
				continue;
			}
			for (int j = 0; j < l2Cache.length; j++) {
				BlockDataObject[][] l3Cache = l2Cache[j];
				if (l3Cache == null) {
					continue;
				}
				for (int k = 0; k < l3Cache.length; k++) {
					BlockDataObject[] l4Cache = l3Cache[k];
					if (l4Cache == null) {
						continue;
					}
					for (int l = 0; l < l4Cache.length; l++) {
						if (l4Cache[l] != null) {
							@SuppressWarnings("unchecked")
							D value = (D) l4Cache[l];
							functionToApply.accept(value);
						}
					}
				}
			}
		}
	}

	public static int modulo(int a) {
		// javas % operator can return negative numbers, which we do not want
		int result = a % L4_Z_SECTION_LENGTH;
		if (result < 0) {
			result += L4_Z_SECTION_LENGTH;
		}
		return result;
	}

	public static int toChunkCoord(int coord) {
		if (coord < 0 && (coord % 16) != 0) {
			return (coord / 16) - 1;
		}
		return coord / 16;
	}

}
