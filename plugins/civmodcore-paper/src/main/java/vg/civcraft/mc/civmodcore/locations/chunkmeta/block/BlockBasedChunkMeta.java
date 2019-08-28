package vg.civcraft.mc.civmodcore.locations.chunkmeta.block;

import org.bukkit.Location;
import org.bukkit.block.Block;

import vg.civcraft.mc.civmodcore.locations.chunkmeta.CacheState;
import vg.civcraft.mc.civmodcore.locations.chunkmeta.ChunkMeta;

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

	protected static final int CHUNK_HEIGHT = 256;
	protected static final int L1_SECTION_COUNT = 16;
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
		System.out.println("Get " + x + " " + y + " "+  z);
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
		return get(modulo(location.getBlockX(), L3_X_SECTION_COUNT), location.getBlockY(),
				modulo(location.getBlockZ(), L4_Z_SECTION_LENGTH));
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
		int yOffsetL1 = y / L1_SECTION_COUNT;
		BlockDataObject<D>[][][] l2Section = getL2SubArray(yOffsetL1, create);
		if (l2Section == null) {
			return null;
		}
		int yOffsetL2 = y % L2_SECTION_COUNT;
		BlockDataObject<D>[][] l3XSection = getL3XSubArray(l2Section, yOffsetL2, create);
		if (l3XSection == null) {
			return null;
		}
		return getL4ZSubArray(l3XSection, x, create);
	}

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
		put(modulo(block.getX(), L3_X_SECTION_COUNT), block.getY(), modulo(block.getZ(), L4_Z_SECTION_LENGTH), blockData, true);
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
		if (blockData == null) {
			throw new IllegalArgumentException("Data may not be null");
		}
		if (isNew) {
			setCacheState(CacheState.MODIFIED);
		}
		System.out.println("Put " + x + " " + y + " "+  z);
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(x, y, true);
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
		put(modulo(location.getBlockX(), L3_X_SECTION_COUNT), location.getBlockY(), modulo(location.getBlockZ(), L4_Z_SECTION_LENGTH),
				blockData, true);
	}

	/**
	 * Removes the entry at the given block if one exists and returns it
	 * 
	 * @param location Block to remove data from, may not be null
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
		BlockDataObject<D>[] l4ZSection = getL4ZSubArrayAbsolute(loc.getBlockX(), loc.getBlockY(), false);
		if (l4ZSection == null) {
			throw new IllegalArgumentException("Can not remove block data from cache, it is already gone");
		}
		if (l4ZSection[loc.getBlockZ()] != blockData) {
			throw new IllegalArgumentException("Can not remove block data from cache, it is already gone");
		}
		l4ZSection[loc.getBlockZ()] = null;
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
			oldData.delete();
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
		return remove(modulo(location.getBlockX(), L3_X_SECTION_COUNT), location.getBlockY(),
				modulo(location.getBlockZ(), L4_Z_SECTION_LENGTH));
	}
	
	public static int modulo(int a, int modulo) {
		//javas % operator can return negative numbers, which we do not want
		int result = a % modulo;
		if (result < 0) {
			result += modulo;
		}
		return result;
	}

}
