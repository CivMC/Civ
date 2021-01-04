package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Tag;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MoreTags;
import vg.civcraft.mc.civmodcore.inventory.items.SpawnEggUtils;
import vg.civcraft.mc.civmodcore.inventory.items.TreeTypeUtils;

/**
 * <p>See <a href="https://github.com/Protonull/BukkitReport/tree/master/reports">BukkitReports</a>.</p>
 *
 * <ul>
 *     <label>See also:</label>
 *     <li>{@link SpawnEggUtils SpawnEggAPI}</li>
 *     <li>{@link TreeTypeUtils TreeTypeAPI}</li>
 * </ul>
 *
 * @deprecated Use {@link MaterialUtils}, {@link ItemUtils}, and {@link MoreTags} instead.
 */
@Deprecated
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

	/**
	 * Checks whether a material would be considered a valid item.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material would be considered a valid item.
	 *
	 * @deprecated Use {@link ItemUtils#isValidItemMaterial(Material)} instead.
	 */
	@Deprecated
	public static boolean isValidItemMaterial(Material material) {
		if (material == null) {
			return false;
		}
		if (material.isAir()) {
			return false;
		}
		if (!material.isItem()) {
			return false;
		}
		return true;
	}

	/**
	 * Attempts to retrieve a material by its slug.
	 *
	 * @param value The value to search for a matching material by.
	 * @return Returns a matched material or null.
	 *
	 * @deprecated Use {@link MaterialUtils#getMaterial(String)} instead.
	 */
	@Deprecated
	public static Material getMaterial(String value) {
		if (Strings.isNullOrEmpty(value)) {
			return null;
		}
		return Material.getMaterial(value.toUpperCase());
	}

	/**
	 * Checks whether a material is air.
	 * Will also return true if the given material is null.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is air.
	 *
	 * @deprecated Use {@link MaterialUtils#isAir(Material)} instead.
	 */
	@Deprecated
	public static boolean isAir(Material material) {
		if (material == null) {
			return true;
		}
		return material.isAir();
	}

	/**
	 * Checks whether a material is a non-stripped log.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a log.
	 *
	 * @deprecated Please use {@code MoreTags.LOGS.isTagged(material);}
	 */
	@Deprecated
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
	 * @deprecated Please use {@code Tag.PLANKS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isPlank(Material material) {
		return Tag.PLANKS.isTagged(material);
	}

	/**
	 * Checks whether a material is a stripped log or wood plank.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a stripped log or wood plank.
	 *
	 * @deprecated Please use {@code MoreTags.STRIPPED_ALL.isTagged(material);}
	 */
	@Deprecated
	public static boolean isStripped(Material material) {
		if (material == null) {
			return false;
		}
		if (isStrippedLog(material)) {
			return true;
		}
		if (isStrippedPlank(material)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks whether a material is a stripped log.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a stripped log.
	 *
	 * @deprecated Please use {@code MoreTags.STRIPPED_LOGS.isTagged(material);}
	 */
	@Deprecated
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

	/**
	 * Checks whether a material is a stripped plank.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a stripped plank.
	 *
	 * @deprecated Please use {@code MoreTags.STRIPPED_PLANKS.isTagged(material);}
	 */
	@Deprecated
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
	 *
	 * @deprecated Please use {@code MoreTags.POTTABLE.isTagged(material);}
	 */
	@Deprecated
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
			case CRIMSON_FUNGUS:
			case CRIMSON_ROOTS:
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
			case WARPED_FUNGUS:
			case WARPED_ROOTS:
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
	 *
	 * @deprecated Please use {@code MoreTags.CROPS.isTagged(material);}
	 */
	@Deprecated
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
	 *
	 * @deprecated Please use {@code MaterialTags.SKULLS.isTagged(material);}
	 */
	@Deprecated
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
	 *
	 * @see Tag#IMPERMEABLE This functionally fulfils glass checking, however the name doesn't incidate that the tag
	 *     is specific to glass, thus the switch remains.
	 *
	 * @deprecated Please use {@code MaterialTags.GLASS.isTagged(material);}
	 */
	@Deprecated
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
	 *
	 * @deprecated Please use {@code MaterialTags.GLASS_PANES.isTagged(material);}
	 */
	@Deprecated
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
	 * @deprecated Please use {@code Tag.DRAGON_IMMUNE.isTagged(material);}
	 */
	@Deprecated
	public static boolean isDragonImmune(Material material) {
		return Tag.DRAGON_IMMUNE.isTagged(material);
	}

	/**
	 * @deprecated Please use {@code Tag.WITHER_IMMUNE.isTagged(material);}
	 */
	@Deprecated
	public static boolean isWitherImmune(Material material) {
		return Tag.WITHER_IMMUNE.isTagged(material);
	}

	/**
	 * @deprecated Please use {@code Tag.FENCE_GATES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isWoodenFenceGate(Material material) {
		return Tag.FENCE_GATES.isTagged(material);
	}

	/**
	 * Checks whether a material is an infested block. This is what used to be referred to as Monster Egg blocks.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is infested.
	 *
	 * @deprecated Please use {@code MaterialTags.INFESTED_BLOCKS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isInfested(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case INFESTED_STONE:
			case INFESTED_COBBLESTONE:
			case INFESTED_STONE_BRICKS:
			case INFESTED_MOSSY_STONE_BRICKS:
			case INFESTED_CRACKED_STONE_BRICKS:
			case INFESTED_CHISELED_STONE_BRICKS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Duplicate of {@link #isWoodenFenceGate(Material)}
	 *
	 * @deprecated Please use {@code Tag.FENCE_GATES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isFenceGate(Material material) {
		return isWoodenFenceGate(material);
	}

	/**
	 * Checks whether a material is a dirt like block.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is dirty.
	 *
	 * @deprecated Please use {@code MoreTags.DIRT.isTagged(material);}
	 */
	@Deprecated
	public static boolean isDirt(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case FARMLAND:
			case GRASS_PATH:
			case GRASS_BLOCK:
			case DIRT:
			case COARSE_DIRT:
			case PODZOL:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a potion of some sort.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a potion.
	 *
	 * @deprecated Please use {@code MoreTags.POTIONS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isPotion(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case POTION:
			case SPLASH_POTION:
			case LINGERING_POTION:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of sword.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a sword.
	 *
	 * @deprecated Please use {@code MaterialTags.SWORDS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isSword(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case WOODEN_SWORD:
			case STONE_SWORD:
			case IRON_SWORD:
			case GOLDEN_SWORD:
			case DIAMOND_SWORD:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of pick axe.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a pick axe.
	 *
	 * @deprecated Please use {@code MaterialTags.PICKAXES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isPickaxe(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case WOODEN_PICKAXE:
			case STONE_PICKAXE:
			case IRON_PICKAXE:
			case GOLDEN_PICKAXE:
			case DIAMOND_PICKAXE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of axe.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a axe.
	 *
	 * @deprecated Please use {@code MaterialTags.AXES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isAxe(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case WOODEN_AXE:
			case STONE_AXE:
			case IRON_AXE:
			case GOLDEN_AXE:
			case DIAMOND_AXE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of spade.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a spade.
	 *
	 * @deprecated Please use {@code MaterialTags.SHOVELS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isShovel(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case WOODEN_SHOVEL:
			case STONE_SHOVEL:
			case IRON_SHOVEL:
			case GOLDEN_SHOVEL:
			case DIAMOND_SHOVEL:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of hoe.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a hoe.
	 *
	 * @deprecated Please use {@code MaterialTags.HOES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isHoe(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case WOODEN_HOE:
			case STONE_HOE:
			case IRON_HOE:
			case GOLDEN_HOE:
			case DIAMOND_HOE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of helmet.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a helmet.
	 *
	 * @deprecated Please use {@code MaterialTags.HELMETS.isTagged(material);}
	 */
	@Deprecated
	public static boolean isHelmet(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case LEATHER_HELMET:
			case CHAINMAIL_HELMET:
			case IRON_HELMET:
			case GOLDEN_HELMET:
			case DIAMOND_HELMET:
			case TURTLE_HELMET:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of chest plate.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a chest plate.
	 *
	 * @deprecated Please use {@code MaterialTags.CHESTPLATES.isTagged(material);}
	 */
	@Deprecated
	public static boolean isChestplate(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case LEATHER_CHESTPLATE:
			case CHAINMAIL_CHESTPLATE:
			case IRON_CHESTPLATE:
			case GOLDEN_CHESTPLATE:
			case DIAMOND_CHESTPLATE:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of leggings.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a pair of leggings.
	 *
	 * @deprecated Please use {@code MaterialTags.LEGGINGS.isTagged(material);}
	 */
	@Deprecated
	public static boolean areLeggings(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case LEATHER_LEGGINGS:
			case CHAINMAIL_LEGGINGS:
			case IRON_LEGGINGS:
			case GOLDEN_LEGGINGS:
			case DIAMOND_LEGGINGS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks whether a material is a type of boots.
	 *
	 * @param material The material to check.
	 * @return Returns true if the material is a pair of boots.
	 *
	 * @deprecated Please use {@code MaterialTags.BOOTS.isTagged(material);}
	 */
	@Deprecated
	public static boolean areBoots(Material material) {
		if (material == null) {
			return false;
		}
		switch (material) {
			case LEATHER_BOOTS:
			case CHAINMAIL_BOOTS:
			case IRON_BOOTS:
			case GOLDEN_BOOTS:
			case DIAMOND_BOOTS:
				return true;
			default:
				return false;
		}
	}

	/**
	 * Gets a random material based on the given objects hashcode.
	 *
	 * @param object Object to base returned material on
	 * @return Material hash of the given object
	 *
	 * @deprecated Use {@link MaterialUtils#getMaterialHash(Object)} instead.
	 */
	@Deprecated
	public static Material getMaterialHash(Object object) {
		if (object == null) {
			return hashMaterials.get(0);
		}
		int index = IntMath.mod(object.hashCode(), hashMaterials.size());
		return hashMaterials.get(index);
	}

}
