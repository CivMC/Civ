package vg.civcraft.mc.civmodcore.inventory.items;

import com.google.common.collect.ImmutableMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.TreeType;

public final class TreeTypeUtils {

	private static final Map<Material, TreeType> TREE_MATERIALS = ImmutableMap.<Material, TreeType>builder()
			// Acacia
			.put(Material.ACACIA_SAPLING, TreeType.ACACIA)
			.put(Material.ACACIA_WOOD, TreeType.ACACIA)
			.put(Material.ACACIA_LOG, TreeType.ACACIA)
			.put(Material.ACACIA_LEAVES, TreeType.ACACIA)
			.put(Material.STRIPPED_ACACIA_LOG, TreeType.ACACIA)
			.put(Material.STRIPPED_ACACIA_WOOD, TreeType.ACACIA)
			// Birch
			.put(Material.BIRCH_SAPLING, TreeType.BIRCH)
			.put(Material.BIRCH_WOOD, TreeType.BIRCH)
			.put(Material.BIRCH_LOG, TreeType.BIRCH)
			.put(Material.BIRCH_LEAVES, TreeType.BIRCH)
			.put(Material.STRIPPED_BIRCH_LOG, TreeType.BIRCH)
			.put(Material.STRIPPED_BIRCH_WOOD, TreeType.BIRCH)
			// Dark Oak
			.put(Material.DARK_OAK_SAPLING, TreeType.DARK_OAK)
			.put(Material.DARK_OAK_WOOD, TreeType.DARK_OAK)
			.put(Material.DARK_OAK_LOG, TreeType.DARK_OAK)
			.put(Material.DARK_OAK_LEAVES, TreeType.DARK_OAK)
			.put(Material.STRIPPED_DARK_OAK_LOG, TreeType.DARK_OAK)
			.put(Material.STRIPPED_DARK_OAK_WOOD, TreeType.DARK_OAK)
			// Jungle
			.put(Material.JUNGLE_SAPLING, TreeType.JUNGLE)
			.put(Material.JUNGLE_WOOD, TreeType.JUNGLE)
			.put(Material.JUNGLE_LOG, TreeType.JUNGLE)
			.put(Material.JUNGLE_LEAVES, TreeType.JUNGLE)
			.put(Material.STRIPPED_JUNGLE_LOG, TreeType.JUNGLE)
			.put(Material.STRIPPED_JUNGLE_WOOD, TreeType.JUNGLE)
			// Oak
			.put(Material.OAK_SAPLING, TreeType.TREE)
			.put(Material.OAK_WOOD, TreeType.TREE)
			.put(Material.OAK_LOG, TreeType.TREE)
			.put(Material.OAK_LEAVES, TreeType.TREE)
			.put(Material.STRIPPED_OAK_LOG, TreeType.TREE)
			.put(Material.STRIPPED_OAK_WOOD, TreeType.TREE)
			// Spruce
			.put(Material.SPRUCE_SAPLING, TreeType.REDWOOD)
			.put(Material.SPRUCE_WOOD, TreeType.REDWOOD)
			.put(Material.SPRUCE_LOG, TreeType.REDWOOD)
			.put(Material.SPRUCE_LEAVES, TreeType.REDWOOD)
			.put(Material.STRIPPED_SPRUCE_LOG, TreeType.REDWOOD)
			.put(Material.STRIPPED_SPRUCE_WOOD, TreeType.REDWOOD)
			// Brown Mushroom
			.put(Material.BROWN_MUSHROOM, TreeType.RED_MUSHROOM)
			.put(Material.BROWN_MUSHROOM_BLOCK, TreeType.BROWN_MUSHROOM)
			// Red Mushroom
			.put(Material.RED_MUSHROOM, TreeType.RED_MUSHROOM)
			.put(Material.RED_MUSHROOM_BLOCK, TreeType.RED_MUSHROOM)
			// Chorus Plants
			.put(Material.CHORUS_FLOWER, TreeType.CHORUS_PLANT)
			.put(Material.CHORUS_PLANT, TreeType.CHORUS_PLANT)
			// Cocoa
			.put(Material.COCOA, TreeType.COCOA_TREE)
			// Crimson Fungus
			.put(Material.CRIMSON_FUNGUS, TreeType.CRIMSON_FUNGUS)
			.put(Material.CRIMSON_STEM, TreeType.CRIMSON_FUNGUS)
			// Crimson Fungus
			.put(Material.WARPED_FUNGUS, TreeType.WARPED_FUNGUS)
			.put(Material.WARPED_STEM, TreeType.CRIMSON_FUNGUS)
			.build();

	private static final Map<TreeType, Material> SAPLING_MATERIALS = ImmutableMap.<TreeType, Material>builder()
			// Acacia
			.put(TreeType.ACACIA, Material.ACACIA_SAPLING)
			// Birch
			.put(TreeType.BIRCH, Material.BIRCH_SAPLING)
			.put(TreeType.TALL_BIRCH, Material.BIRCH_SAPLING)
			// Dark Oak
			.put(TreeType.DARK_OAK, Material.DARK_OAK_SAPLING)
			// Jungle
			.put(TreeType.JUNGLE, Material.JUNGLE_SAPLING)
			.put(TreeType.SMALL_JUNGLE, Material.JUNGLE_SAPLING)
			.put(TreeType.JUNGLE_BUSH, Material.JUNGLE_SAPLING)
			// Oak
			.put(TreeType.BIG_TREE, Material.OAK_SAPLING)
			.put(TreeType.TREE, Material.OAK_SAPLING)
			.put(TreeType.SWAMP, Material.OAK_SAPLING)
			// Spruce
			.put(TreeType.MEGA_REDWOOD, Material.SPRUCE_SAPLING)
			.put(TreeType.REDWOOD, Material.SPRUCE_SAPLING)
			.put(TreeType.TALL_REDWOOD, Material.SPRUCE_SAPLING)
			// Brown Mushroom
			.put(TreeType.BROWN_MUSHROOM, Material.BROWN_MUSHROOM)
			// Red Mushroom
			.put(TreeType.RED_MUSHROOM, Material.RED_MUSHROOM)
			// Chorus Plants
			.put(TreeType.CHORUS_PLANT, Material.CHORUS_PLANT)
			// Cocoa
			.put(TreeType.COCOA_TREE, Material.COCOA)
			// Crimson Fungus
			.put(TreeType.CRIMSON_FUNGUS, Material.CRIMSON_FUNGUS)
			// Crimson Fungus
			.put(TreeType.WARPED_FUNGUS, Material.WARPED_FUNGUS)
			.build();

	public static void init() {
		// Determine if there's any tree types missing
		{
			final Set<TreeType> missing = new HashSet<>();
			final Set<TreeType> exclude = Set.of( // Set of TreeTypes that cannot be reverse searched
					TreeType.BIG_TREE, TreeType.JUNGLE_BUSH, TreeType.SWAMP, TreeType.SMALL_JUNGLE,
					TreeType.TALL_BIRCH, TreeType.MEGA_REDWOOD, TreeType.TALL_REDWOOD);
			CollectionUtils.addAll(missing, TreeType.values());
			missing.removeIf(type -> exclude.contains(type) || TREE_MATERIALS.containsValue(type));
			if (!missing.isEmpty()) {
				Bukkit.getLogger().warning("[TreeTypeUtils] The following tree types are missing: " +
						missing.stream().map(Enum::name).collect(Collectors.joining(",")) + ".");
			}
		}
		// Determine if there's any sapling types missing
		{
			final Set<TreeType> missing = new HashSet<>();
			CollectionUtils.addAll(missing, TreeType.values());
			missing.removeIf(SAPLING_MATERIALS::containsKey);
			if (!missing.isEmpty()) {
				Bukkit.getLogger().warning("[TreeTypeUtils] The following sapling types are missing: " +
						missing.stream().map(Enum::name).collect(Collectors.joining(",")) + ".");
			}
		}
	}


	public static TreeType getMatchingTreeType(final Material material) {
		return TREE_MATERIALS.get(material);
	}
	
	public static Material getMatchingSapling(final TreeType type) {
		return SAPLING_MATERIALS.get(type);
	}

}
