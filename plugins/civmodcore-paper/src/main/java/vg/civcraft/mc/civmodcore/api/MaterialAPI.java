package vg.civcraft.mc.civmodcore.api;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Tag;

import com.google.common.math.IntMath;

/**
 * Class of static APIs for Materials. Some material functions are located on classes more suited for them, such as
 * {@link SpawnEggAPI#isSpawnEgg(Material) SpawnEggAPI.isSpawnEgg()}, but is also a supplement to Bukkit's
 * {@link org.bukkit.Tag Tag} class that fulfills a similar function.
 * */
public final class MaterialAPI {
	
	
	private static final List<Material> hashMaterials = new ArrayList<>();
	
	static {
		hashMaterials.addAll(Tag.WOOL.getValues());
		hashMaterials.add(Material.BLACK_STAINED_GLASS);
		hashMaterials.add(Material.WHITE_STAINED_GLASS);
		hashMaterials.add(Material.YELLOW_STAINED_GLASS);
		hashMaterials.add(Material.RED_STAINED_GLASS);
		hashMaterials.add(Material.LIME_STAINED_GLASS);
		hashMaterials.add(Material.GRAY_STAINED_GLASS);
		hashMaterials.add(Material.BLUE_STAINED_GLASS);
		hashMaterials.add(Material.LIGHT_GRAY_STAINED_GLASS);
		hashMaterials.add(Material.LIGHT_BLUE_STAINED_GLASS);
		hashMaterials.add(Material.GREEN_STAINED_GLASS);
		hashMaterials.add(Material.BROWN_STAINED_GLASS);
		hashMaterials.add(Material.PINK_STAINED_GLASS);
		hashMaterials.add(Material.PURPLE_STAINED_GLASS);
		hashMaterials.add(Material.CYAN_STAINED_GLASS);
		hashMaterials.add(Material.MAGENTA_STAINED_GLASS);
		hashMaterials.add(Material.ORANGE_STAINED_GLASS);
		hashMaterials.add(Material.BLACK_STAINED_GLASS_PANE);
		hashMaterials.add(Material.WHITE_STAINED_GLASS_PANE);
		hashMaterials.add(Material.YELLOW_STAINED_GLASS_PANE);
		hashMaterials.add(Material.RED_STAINED_GLASS_PANE);
		hashMaterials.add(Material.LIME_STAINED_GLASS_PANE);
		hashMaterials.add(Material.GRAY_STAINED_GLASS_PANE);
		hashMaterials.add(Material.BLUE_STAINED_GLASS_PANE);
		hashMaterials.add(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
		hashMaterials.add(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
		hashMaterials.add(Material.GREEN_STAINED_GLASS_PANE);
		hashMaterials.add(Material.BROWN_STAINED_GLASS_PANE);
		hashMaterials.add(Material.PINK_STAINED_GLASS_PANE);
		hashMaterials.add(Material.PURPLE_STAINED_GLASS_PANE);
		hashMaterials.add(Material.CYAN_STAINED_GLASS_PANE);
		hashMaterials.add(Material.MAGENTA_STAINED_GLASS_PANE);
		hashMaterials.add(Material.ORANGE_STAINED_GLASS_PANE);
		hashMaterials.add(Material.BLACK_CONCRETE);
		hashMaterials.add(Material.WHITE_CONCRETE);
		hashMaterials.add(Material.YELLOW_CONCRETE);
		hashMaterials.add(Material.RED_CONCRETE);
		hashMaterials.add(Material.LIME_CONCRETE);
		hashMaterials.add(Material.GRAY_CONCRETE);
		hashMaterials.add(Material.BLUE_CONCRETE);
		hashMaterials.add(Material.LIGHT_GRAY_CONCRETE);
		hashMaterials.add(Material.LIGHT_BLUE_CONCRETE);
		hashMaterials.add(Material.GREEN_CONCRETE);
		hashMaterials.add(Material.BROWN_CONCRETE);
		hashMaterials.add(Material.PINK_CONCRETE);
		hashMaterials.add(Material.PURPLE_CONCRETE);
		hashMaterials.add(Material.CYAN_CONCRETE);
		hashMaterials.add(Material.MAGENTA_CONCRETE);
		hashMaterials.add(Material.ORANGE_CONCRETE);
	}

	private MaterialAPI() { } // Make the class effectively static

	/**
	 * Checks whether a material is air.
	 * Will also return true if the given material is null.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is air.
	 * */
	public static boolean isAir(Material material) {
		if (material == null) {
			return true;
		}
		switch (material) {
			case AIR:
			case CAVE_AIR:
			case VOID_AIR:
				return true;
			default:
				break;
		}
		return false;
	}

	/**
	 * Checks whether a material is a log.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a log.
	 * */
	public static boolean isLog(Material material) {
		if (material == null) {
			return false;
		}
		if (isStrippedLog(material)) {
			return true;
		}
		switch (material) {
			case ACACIA_LOG:
			case BIRCH_LOG:
			case DARK_OAK_LOG:
			case JUNGLE_LOG:
			case OAK_LOG:
			case SPRUCE_LOG:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a wood plank.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a wood plank.
	 * */
	public static boolean isPlank(Material material) {
		if (material == null) {
			return false;
		}
		if (isStrippedPlank(material)) {
			return true;
		}
		switch (material) {
			case ACACIA_WOOD:
			case BIRCH_WOOD:
			case DARK_OAK_WOOD:
			case JUNGLE_WOOD:
			case OAK_WOOD:
			case SPRUCE_WOOD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a stripped log or wood plank.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a stripped log or wood plank.
	 * */
	public static boolean isStripped(Material material) {
		if (material == null) {
			return false;
		}
		return isStrippedLog(material) || isStrippedPlank(material);
	}

	public static boolean isStrippedLog(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case STRIPPED_ACACIA_LOG:
			case STRIPPED_BIRCH_LOG:
			case STRIPPED_DARK_OAK_LOG:
			case STRIPPED_JUNGLE_LOG:
			case STRIPPED_OAK_LOG:
			case STRIPPED_SPRUCE_LOG:
				return true;
			default:
				return false;
		}
	}

	public static boolean isStrippedPlank(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case STRIPPED_ACACIA_WOOD:
			case STRIPPED_BIRCH_WOOD:
			case STRIPPED_DARK_OAK_WOOD:
			case STRIPPED_JUNGLE_WOOD:
			case STRIPPED_OAK_WOOD:
			case STRIPPED_SPRUCE_WOOD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material can be placed into a pot.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material can be potted.
	 * */
	public static boolean isPottable(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case ACACIA_SAPLING:
			case ALLIUM:
			case AZURE_BLUET:
			case BAMBOO:
			case BIRCH_SAPLING:
			case BLUE_ORCHID:
			case BROWN_MUSHROOM:
			case CACTUS:
			case CORNFLOWER:
			case DANDELION:
			case DARK_OAK_SAPLING:
			case DEAD_BUSH:
			case FERN:
			case JUNGLE_SAPLING:
			case LILY_OF_THE_VALLEY:
			case OAK_SAPLING:
			case ORANGE_TULIP:
			case OXEYE_DAISY:
			case PINK_TULIP:
			case POPPY:
			case RED_MUSHROOM:
			case RED_TULIP:
			case SPRUCE_SAPLING:
			case WHITE_TULIP:
			case WITHER_ROSE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a crop. Something is a crop if it's a plant that can grow, excluding Saplings.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a crop.
	 *
	 * @see org.bukkit.block.data.Ageable Check if the Block's data is an instance of this Ageable, though be aware that
	 * {@link Material#FIRE fire} and {@link Material#FROSTED_ICE frosted ice} also implement Ageable.
	 * */
	public static boolean isCrop(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case BAMBOO:
			case BEETROOTS:
			case CACTUS:
			case CARROTS:
			case CHORUS_FLOWER:
			case CHORUS_PLANT:
			case COCOA:
			case KELP:
			case MELON_STEM:
			case NETHER_WART:
			case POTATOES:
			case PUMPKIN_STEM:
			case SUGAR_CANE:
			case SWEET_BERRY_BUSH:
			case WHEAT:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a skull/head.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a skull/head.
	 * */
	public static boolean isSkull(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case CREEPER_HEAD:
			case CREEPER_WALL_HEAD:
			case DRAGON_HEAD:
			case DRAGON_WALL_HEAD:
			case PLAYER_HEAD:
			case PLAYER_WALL_HEAD:
			case ZOMBIE_HEAD:
			case ZOMBIE_WALL_HEAD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a glass block, coloured or otherwise.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a glass block.
	 * */
	public static boolean isGlassBlock(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case BLACK_STAINED_GLASS:
			case BLUE_STAINED_GLASS:
			case BROWN_STAINED_GLASS:
			case CYAN_STAINED_GLASS:
			case GRAY_STAINED_GLASS:
			case GLASS:
			case GREEN_STAINED_GLASS:
			case LIGHT_BLUE_STAINED_GLASS:
			case LIGHT_GRAY_STAINED_GLASS:
			case LIME_STAINED_GLASS:
			case MAGENTA_STAINED_GLASS:
			case ORANGE_STAINED_GLASS:
			case PINK_STAINED_GLASS:
			case PURPLE_STAINED_GLASS:
			case RED_STAINED_GLASS:
			case WHITE_STAINED_GLASS:
			case YELLOW_STAINED_GLASS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a glass pane, coloured or otherwise.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a glass pane.
	 * */
	public static boolean isGlassPane(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case BLACK_STAINED_GLASS_PANE:
			case BLUE_STAINED_GLASS_PANE:
			case BROWN_STAINED_GLASS_PANE:
			case CYAN_STAINED_GLASS_PANE:
			case GRAY_STAINED_GLASS_PANE:
			case GLASS_PANE:
			case GREEN_STAINED_GLASS_PANE:
			case LIGHT_BLUE_STAINED_GLASS_PANE:
			case LIGHT_GRAY_STAINED_GLASS_PANE:
			case LIME_STAINED_GLASS_PANE:
			case MAGENTA_STAINED_GLASS_PANE:
			case ORANGE_STAINED_GLASS_PANE:
			case PINK_STAINED_GLASS_PANE:
			case PURPLE_STAINED_GLASS_PANE:
			case RED_STAINED_GLASS_PANE:
			case WHITE_STAINED_GLASS_PANE:
			case YELLOW_STAINED_GLASS_PANE:
				return true;
			default:
				return false;
		}
	}
	
	/**
	 * Gets a random material based on the given objects hashcode
	 * @param o Object to base returned material on
	 * @return Material hash of the given object
	 */
	public static Material getMaterialHash(Object o) {
		if (o == null) {
			return hashMaterials.get(0);
		}
		int hash = o.hashCode();
		int index = IntMath.mod(hash, hashMaterials.size());
		return hashMaterials.get(index);
	}

}
