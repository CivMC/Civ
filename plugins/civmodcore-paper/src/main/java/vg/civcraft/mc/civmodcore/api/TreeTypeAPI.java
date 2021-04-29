package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Material;
import org.bukkit.TreeType;
import vg.civcraft.mc.civmodcore.inventory.items.TreeTypeUtils;

/**
 * @deprecated Use {@link TreeTypeUtils} instead.
 */
@Deprecated
public final class TreeTypeAPI {

	/**
	 * @deprecated Use {@link TreeTypeUtils#getMatchingTreeType(Material)} instead.
	 */
	@Deprecated
	public static TreeType getMatchingTreeType(Material material) {
		if (material == null) {
			return null;
		}
		switch (material) {
			case ACACIA_SAPLING:
			case ACACIA_WOOD:
			case ACACIA_LOG:
			case ACACIA_LEAVES:
			case STRIPPED_ACACIA_LOG:
			case STRIPPED_ACACIA_WOOD:
				return TreeType.ACACIA;
			case BIRCH_SAPLING:
			case BIRCH_WOOD:
			case BIRCH_LOG:
			case BIRCH_LEAVES:
			case STRIPPED_BIRCH_LOG:
			case STRIPPED_BIRCH_WOOD:
				return TreeType.BIRCH;
			case OAK_SAPLING:
			case OAK_WOOD:
			case OAK_LOG:
			case OAK_LEAVES:
			case STRIPPED_OAK_LOG:
			case STRIPPED_OAK_WOOD:
				return TreeType.TREE;
			case JUNGLE_SAPLING:
			case JUNGLE_WOOD:
			case JUNGLE_LOG:
			case JUNGLE_LEAVES:
			case STRIPPED_JUNGLE_LOG:
			case STRIPPED_JUNGLE_WOOD:
				return TreeType.JUNGLE;
			case DARK_OAK_SAPLING:
			case DARK_OAK_WOOD:
			case DARK_OAK_LOG:
			case DARK_OAK_LEAVES:
			case STRIPPED_DARK_OAK_LOG:
			case STRIPPED_DARK_OAK_WOOD:
				return TreeType.DARK_OAK;
			case SPRUCE_SAPLING:
			case SPRUCE_WOOD:
			case SPRUCE_LOG:
			case SPRUCE_LEAVES:
			case STRIPPED_SPRUCE_LOG:
			case STRIPPED_SPRUCE_WOOD:
				return TreeType.REDWOOD;
			case CHORUS_FLOWER:
			case CHORUS_PLANT:
				return TreeType.CHORUS_PLANT;
			case RED_MUSHROOM:
			case RED_MUSHROOM_BLOCK:
				return TreeType.RED_MUSHROOM;
			case BROWN_MUSHROOM:
			case BROWN_MUSHROOM_BLOCK:
				return TreeType.BROWN_MUSHROOM;
			case COCOA:
				return TreeType.COCOA_TREE;
			default:
				return null;
		}
	}

	/**
	 * @deprecated Use {@link TreeTypeUtils#getMatchingSapling(TreeType)} instead.
	 */
	@Deprecated
	public static Material getMatchingSapling(TreeType type) {
		if (type == null) {
			return null;
		}
		switch(type) {
			case ACACIA:
				return Material.ACACIA_SAPLING;
			case BIG_TREE:
			case TREE:
			case SWAMP:
				return Material.OAK_SAPLING;
			case BIRCH:
			case TALL_BIRCH:
				return Material.BIRCH_SAPLING;
			case BROWN_MUSHROOM:
				return Material.BROWN_MUSHROOM;
			case CHORUS_PLANT:
				return Material.CHORUS_PLANT;
			case COCOA_TREE:
				return Material.COCOA;
			case DARK_OAK:
				return Material.DARK_OAK_SAPLING;
			case JUNGLE:
			case SMALL_JUNGLE:
			case JUNGLE_BUSH:
				return Material.JUNGLE_SAPLING;
			case MEGA_REDWOOD:
			case REDWOOD:
			case TALL_REDWOOD:
				return Material.SPRUCE_SAPLING;
			case RED_MUSHROOM:
				return Material.RED_MUSHROOM;
			default:
				return null;
		}
	}

}
