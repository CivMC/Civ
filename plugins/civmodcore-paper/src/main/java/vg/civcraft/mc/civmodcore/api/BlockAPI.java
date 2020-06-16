package vg.civcraft.mc.civmodcore.api;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.server.v1_14_R1.BlockProperties;
import net.minecraft.server.v1_14_R1.BlockState;
import net.minecraft.server.v1_14_R1.IBlockState;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_14_R1.block.CraftBlock;
import org.bukkit.util.BlockIterator;

/**
 * Class of utility functions for Blocks, and BlockFaces referencing Blocks around a Block.
 */
public final class BlockAPI {
	
	private BlockAPI() { }

	public static final List<BlockFace> ALL_SIDES = ImmutableList.of(
			BlockFace.UP,
			BlockFace.DOWN,
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.EAST);

	public static final List<BlockFace> PLANAR_SIDES = ImmutableList.of(
			BlockFace.NORTH,
			BlockFace.SOUTH,
			BlockFace.WEST,
			BlockFace.EAST);
	
	private static final Map<String, BlockState<?>> blockStateByIdentifier = new HashMap<>();
	
	static  {
		for(Field field : BlockProperties.class.getFields()) {
			if (!BlockState.class.isAssignableFrom(field.getType())) {
				continue;
			}
			field.setAccessible(true);
			BlockState<?> bs;
			try {
				bs = (BlockState<?>)field.get(null);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
				continue;
			}
			//when updating, search for the method returning the string given in the constructor
			String key = bs.a();
			blockStateByIdentifier.put(key, bs);
		}
	}

	/**
	 * <p>Checks whether this block is valid and so can be handled reasonably without error.</p>
	 *
	 * <p>Note: This will return true even if the block is air. Use {@link MaterialAPI#isAir(Material)} as an
	 * additional check if this is important to you.</p>
	 *
	 * @param block The block to check.
	 * @return Returns true if the block is valid.
	 */
	public static boolean isValidBlock(Block block) {
		if (block == null) {
			return false;
		}
		if (block.getType() == null) { // Do not remove this, it's not necessarily certain
			return false;
		}
		return LocationAPI.isValidLocation(block.getLocation());
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces An array of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSidesMapped(Block block, BlockFace... faces) {
		if (faces == null || faces.length < 1) {
			return Collections.unmodifiableMap(new EnumMap<>(BlockFace.class));
		}
		else {
			return getBlockSidesMapped(block, Arrays.asList(faces));
		}
	}

	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static Map<BlockFace, Block> getBlockSidesMapped(Block block, Collection<BlockFace> faces) {
		EnumMap<BlockFace, Block> results = new EnumMap<>(BlockFace.class);
		if (block != null && faces != null) {
			faces.forEach(face -> results.put(face, block.getRelative(face)));
		}
		return Collections.unmodifiableMap(results);
	}
	
	/**
	 * Returns a map of a block's relatives.
	 *
	 * @param block The block to get the relatives of.
	 * @param faces A collection of the faces, which will be the keys of the returned map.
	 * @return Returns an immutable map of the block's relatives.
	 */
	public static List<Block> getBlockSides(Block block, Collection<BlockFace> faces) {
		if (block == null || faces == null) {
            throw new IllegalArgumentException("One of the args passed was null");
        }
		if (faces.isEmpty()) {
			return Collections.emptyList();
		}
        return faces.stream().map(block::getRelative).collect(Collectors.toList());
	}

	/**
	 * Returns a map of all the block's relatives.
	 *
	 * @param block The block to get all the relatives of.
	 * @return Returns an immutable map of all the block's relatives.
	 */
	public static Map<BlockFace, Block> getAllSidesMapped(Block block) {
		return getBlockSidesMapped(block, ALL_SIDES);
	}
	
	/**
	 * Returns a list of all the block's relatives.
	 *
	 * @param block The block to get all the relatives of.
	 * @return Returns an immutable list of all the block's relatives.
	 */
	public static List<Block> getAllSides(Block block) {
		return getBlockSides(block, ALL_SIDES);
	}

	/**
	 * Returns a map of all the block's planar relatives.
	 *
	 * @param block The block to get the planar relatives of.
	 * @return Returns an immutable map of all the block's planar relatives.
	 */
	public static Map<BlockFace, Block> getPlanarSidesMapped(Block block) {
		return getBlockSidesMapped(block, PLANAR_SIDES);
	}
	
	/**
	 * Returns a list of all the block's planar relatives.
	 *
	 * @param block The block to get the planar relatives of.
	 * @return Returns an immutable list of all the block's planar relatives.
	 */
	public static List<Block> getPlanarSides(Block block) {
		return getBlockSides(block, PLANAR_SIDES);
	}

	/**
	 * Turns once in a clockwise direction.
	 *
	 * @param face The starting face, which must exist and be planar.
	 * @return Returns the next planar face in a clockwise direction.
	 *
	 * @exception IllegalArgumentException Throws if the given face is null or non-planar.
	 */
	public static BlockFace turnClockwise(BlockFace face) {
		Preconditions.checkArgument(face != null);
		Preconditions.checkArgument(PLANAR_SIDES.contains(face));
		switch (face) {
			case NORTH:
				return BlockFace.EAST;
			case EAST:
				return BlockFace.SOUTH;
			case SOUTH:
				return BlockFace.WEST;
			default:
			case WEST:
				return BlockFace.NORTH;
		}
	}

	/**
	 * Turns once in a anti-clockwise direction.
	 *
	 * @param face The starting face, which must exist and be planar.
	 * @return Returns the next planar face in a anti-clockwise direction.
	 *
	 * @exception IllegalArgumentException Throws if the given face is null or non-planar.
	 */
	public static BlockFace turnAntiClockwise(BlockFace face) {
		Preconditions.checkArgument(face != null);
		Preconditions.checkArgument(PLANAR_SIDES.contains(face));
		switch (face) {
			case NORTH:
				return BlockFace.WEST;
			case EAST:
				return BlockFace.NORTH;
			case SOUTH:
				return BlockFace.EAST;
			default:
			case WEST:
				return BlockFace.SOUTH;
		}
	}

	/**
	 * Attempts to get the other block of a double chest.
	 *
	 * @param block The block that represents the double chest block you already have.
	 * @return Returns the other block or null if none can be found, or if the given block isn't that of a double chest.
	 */
	public static Block getOtherDoubleChestBlock(Block block) {
		if (!isValidBlock(block)) {
			return null;
		}
		Chest chest = chain(() -> (Chest) block.getBlockData());
		if (chest == null) {
			return null;
		}
		switch (chest.getType()) {
			case LEFT: // This block is left side
				return block.getRelative(turnClockwise(chest.getFacing()));
			case RIGHT:
				return block.getRelative(turnAntiClockwise(chest.getFacing()));
			default:
			case SINGLE:
				return null;
		}
	}

	/**
	 * Creates a {@link BlockIterator} from a block's perspective, which is lacking from its constructors, which are
	 * more focused on entities.
	 *
	 * @param block The block to start the iterator from.
	 * @param face The direction at which the iterator should iterate.
	 * @param range The distance the iterator should iterate.
	 * @return Returns a new instance of an BlockIterator.
	 */
	public static BlockIterator getBlockIterator(Block block, BlockFace face, int range) {
		if (!BlockAPI.isValidBlock(block)) {
			throw new IllegalArgumentException("Cannot create a block iterator from a null block.");
		}
		if (face == null || face == BlockFace.SELF) {
			throw new IllegalArgumentException("Block iterator requires a valid direction.");
		}
		if (range <= 0) {
			throw new IllegalArgumentException("Block iterator requires a range of 1 or higher.");
		}
		return new BlockIterator(block.getWorld(), block.getLocation().toVector(), face.getDirection(), 0, range);
	}

	public static boolean setBlockProperty(Block block, String key, String value) {
		//we need this wrapper method to trick the java generics
		return innerSetBlockProperty(block, key, value);
	}

	// WHY IS THIS PUBLIC IF IT'S INNER?
	public static <V extends Comparable<V>> boolean innerSetBlockProperty(Block block, String key, String value) {
		@SuppressWarnings("unchecked")
		IBlockState<V> state = (IBlockState<V>) blockStateByIdentifier.get(key);
		if (state == null) {
			return false;
		}
		Optional<V> opt = state.b(value);
		if (!opt.isPresent()) {
			return false;
		}
		V valueToSet = state.b(value).get();
		CraftBlock cb = (CraftBlock) block;
		CraftWorld world = (CraftWorld) block.getWorld();
		//no idea what the last integer parameter does, I found 2 and 3 being used in NMS code and stuck to that
		world.getHandle().setTypeAndData(cb.getPosition(), cb.getNMS().set( state, valueToSet), 2);
		return true;
	}

}
