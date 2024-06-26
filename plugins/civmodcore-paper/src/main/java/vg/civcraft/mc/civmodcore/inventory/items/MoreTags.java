package vg.civcraft.mc.civmodcore.inventory.items;

import com.destroystokyo.paper.MaterialTags;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Tag;
import org.bukkit.block.data.Ageable;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

/**
 * Fills in the gaps between {@link Tag} and {@link MaterialTags}.
 */
public final class MoreTags {

    /**
     * This differs from {@link Tag#LOGS} as that includes every type of wood,
     * including stripped logs and all planks.
     */
    public static final Tag<Material> LOGS = new BetterTag<>("logs",
        ImmutableSet.<Material>builder()
            .add(Material.ACACIA_LOG)
            .add(Material.BIRCH_LOG)
            .add(Material.CHERRY_LOG)
            .add(Material.DARK_OAK_LOG)
            .add(Material.JUNGLE_LOG)
            .add(Material.MANGROVE_LOG)
            .add(Material.OAK_LOG)
            .add(Material.SPRUCE_LOG)
            .build());

    public static final Tag<Material> STRIPPED_LOGS = new BetterTag<>("stripped_logs",
        ImmutableSet.<Material>builder()
            .add(Material.STRIPPED_ACACIA_LOG)
            .add(Material.STRIPPED_BIRCH_LOG)
            .add(Material.STRIPPED_CHERRY_LOG)
            .add(Material.STRIPPED_DARK_OAK_LOG)
            .add(Material.STRIPPED_JUNGLE_LOG)
            .add(Material.STRIPPED_MANGROVE_LOG)
            .add(Material.STRIPPED_OAK_LOG)
            .add(Material.STRIPPED_SPRUCE_LOG)
            .build());

    public static final Tag<Material> STRIPPED_PLANKS = new BetterTag<>("stripped_planks",
        ImmutableSet.<Material>builder()
            .add(Material.STRIPPED_ACACIA_WOOD)
            .add(Material.STRIPPED_BIRCH_WOOD)
            .add(Material.STRIPPED_CHERRY_WOOD)
            .add(Material.STRIPPED_DARK_OAK_WOOD)
            .add(Material.STRIPPED_JUNGLE_WOOD)
            .add(Material.STRIPPED_MANGROVE_WOOD)
            .add(Material.STRIPPED_OAK_WOOD)
            .add(Material.STRIPPED_SPRUCE_WOOD)
            .build());

    public static final Tag<Material> STRIPPED_ALL = new BetterTag<>("stripped_all",
        ImmutableSet.<Material>builder()
            .addAll(STRIPPED_LOGS.getValues())
            .addAll(STRIPPED_PLANKS.getValues())
            .build());

    public static final Tag<Material> DIRT = new BetterTag<>("dirt",
        ImmutableSet.<Material>builder()
            .add(Material.FARMLAND)
            .add(Material.DIRT_PATH)
            .add(Material.GRASS_BLOCK)
            .add(Material.DIRT)
            .add(Material.COARSE_DIRT)
            .add(Material.PODZOL)
            .add(Material.ROOTED_DIRT)
            .build());

    public static final Tag<Material> POTIONS = new BetterTag<>("potion",
        ImmutableSet.<Material>builder()
            .add(Material.POTION)
            .add(Material.SPLASH_POTION)
            .add(Material.LINGERING_POTION)
            .build());

    public static final Tag<Material> DUSTABLE = new BetterTag<>("dustable",
        ImmutableSet.<Material>builder()
            .add(Material.SUSPICIOUS_GRAVEL)
            .add(Material.SUSPICIOUS_SAND)
            .build());

    /**
     * Materials of items that can apply potion effects.
     */
    public static final Tag<Material> EFFECTORS = new BetterTag<>("effectors",
        ImmutableSet.<Material>builder()
            .addAll(POTIONS.getValues())
            .add(Material.TIPPED_ARROW)
            .build());

    /**
     * This is necessary as {@link Tag#CROPS} is crap and only has a few crops, and {@link Ageable} includes none-crop
     * blocks like {@link Material#FIRE fire} and {@link Material#FROSTED_ICE frosted ice}.
     */
    public static final Tag<Material> CROPS = new BetterTag<>("crops",
        ImmutableSet.<Material>builder()
            .add(Material.BAMBOO)
            .add(Material.BAMBOO_SAPLING)
            .add(Material.BEETROOTS)
            .add(Material.CACTUS)
            .add(Material.CARROTS)
            .add(Material.CHORUS_FLOWER)
            .add(Material.CHORUS_PLANT)
            .add(Material.COCOA)
            .add(Material.KELP)
            .add(Material.MELON_STEM)
            .add(Material.NETHER_WART)
            .add(Material.PITCHER_CROP)
            .add(Material.PITCHER_PLANT)
            .add(Material.POTATOES)
            .add(Material.PUMPKIN_STEM)
            .add(Material.SUGAR_CANE)
            .add(Material.SWEET_BERRY_BUSH)
            .add(Material.TWISTING_VINES)
            .add(Material.TORCHFLOWER_CROP)
            .add(Material.TORCHFLOWER)
            .add(Material.WEEPING_VINES)
            .add(Material.WHEAT)
            .add(Material.CAVE_VINES)
            .build());

    public static final Tag<Material> POTTABLE = new BetterTag<>("pottable",
        ImmutableSet.<Material>builder()
            .add(Material.ACACIA_SAPLING)
            .add(Material.ALLIUM)
            .add(Material.AZURE_BLUET)
            .add(Material.BAMBOO)
            .add(Material.BIRCH_SAPLING)
            .add(Material.BLUE_ORCHID)
            .add(Material.BROWN_MUSHROOM)
            .add(Material.CACTUS)
            .add(Material.CHERRY_SAPLING)
            .add(Material.CORNFLOWER)
            .add(Material.CRIMSON_FUNGUS)
            .add(Material.CRIMSON_ROOTS)
            .add(Material.DANDELION)
            .add(Material.DARK_OAK_SAPLING)
            .add(Material.DEAD_BUSH)
            .add(Material.FERN)
            .add(Material.JUNGLE_SAPLING)
            .add(Material.LILY_OF_THE_VALLEY)
            .add(Material.MANGROVE_PROPAGULE)
            .add(Material.OAK_SAPLING)
            .add(Material.ORANGE_TULIP)
            .add(Material.OXEYE_DAISY)
            .add(Material.PINK_TULIP)
            .add(Material.POPPY)
            .add(Material.RED_MUSHROOM)
            .add(Material.RED_TULIP)
            .add(Material.SPRUCE_SAPLING)
            .add(Material.WARPED_FUNGUS)
            .add(Material.WARPED_ROOTS)
            .add(Material.WHITE_TULIP)
            .add(Material.WITHER_ROSE)
            .build());

    public static final Tag<Material> NETHERITE_ARMOUR = new BetterTag<>("netherite_armour",
        ImmutableSet.<Material>builder()
            .add(Material.NETHERITE_HELMET)
            .add(Material.NETHERITE_CHESTPLATE)
            .add(Material.NETHERITE_LEGGINGS)
            .add(Material.NETHERITE_BOOTS)
            .build());

    /**
     * Includes {@link Material#NETHERITE_SWORD} as while it's not strictly a tool, it's likely to be included in
     * similar checks for handheld usable items, and best to support that and not be awkward for the sake of
     * technical correctness.
     */
    public static final Tag<Material> NETHERITE_TOOLS = new BetterTag<>("netherite_tools",
        ImmutableSet.<Material>builder()
            .add(Material.NETHERITE_SWORD)
            .add(Material.NETHERITE_PICKAXE)
            .add(Material.NETHERITE_AXE)
            .add(Material.NETHERITE_SHOVEL)
            .add(Material.NETHERITE_HOE)
            .build());

    public static final Tag<Material> NETHERITE_ITEMS = new BetterTag<>("netherite_items",
        ImmutableSet.<Material>builder()
            .addAll(NETHERITE_ARMOUR.getValues())
            .addAll(NETHERITE_TOOLS.getValues())
            .add(Material.NETHERITE_INGOT)
            .add(Material.NETHERITE_BLOCK)
            .add(Material.NETHERITE_SCRAP)
            .build());

    public static final Tag<Material> LIGHTABLE_CANDLES = new BetterTag<>("lightable_candles",
        ImmutableSet.<Material>builder()
            .add(Material.CANDLE)
            .add(Material.CANDLE_CAKE)
            .add(Material.CYAN_CANDLE)
            .add(Material.CYAN_CANDLE_CAKE)
            .add(Material.BLACK_CANDLE)
            .add(Material.BLACK_CANDLE_CAKE)
            .add(Material.BLUE_CANDLE)
            .add(Material.BLUE_CANDLE_CAKE)
            .add(Material.BROWN_CANDLE)
            .add(Material.BROWN_CANDLE_CAKE)
            .add(Material.GRAY_CANDLE)
            .add(Material.GRAY_CANDLE_CAKE)
            .add(Material.GREEN_CANDLE)
            .add(Material.GREEN_CANDLE_CAKE)
            .add(Material.LIGHT_BLUE_CANDLE)
            .add(Material.LIGHT_BLUE_CANDLE_CAKE)
            .add(Material.LIGHT_GRAY_CANDLE)
            .add(Material.LIGHT_GRAY_CANDLE_CAKE)
            .add(Material.LIME_CANDLE)
            .add(Material.LIME_CANDLE_CAKE)
            .add(Material.MAGENTA_CANDLE)
            .add(Material.MAGENTA_CANDLE_CAKE)
            .add(Material.ORANGE_CANDLE)
            .add(Material.ORANGE_CANDLE_CAKE)
            .add(Material.PINK_CANDLE)
            .add(Material.PINK_CANDLE_CAKE)
            .add(Material.PURPLE_CANDLE)
            .add(Material.PURPLE_CANDLE_CAKE)
            .add(Material.RED_CANDLE)
            .add(Material.RED_CANDLE_CAKE)
            .add(Material.WHITE_CANDLE)
            .add(Material.WHITE_CANDLE_CAKE)
            .add(Material.YELLOW_CANDLE)
            .add(Material.YELLOW_CANDLE_CAKE)
            .build());

    public static final Tag<Material> COPPER_BLOCKS = new BetterTag<>("copper_blocks",
        ImmutableSet.<Material>builder()
            .add(Material.COPPER_BLOCK)
            .add(Material.EXPOSED_COPPER)
            .add(Material.WEATHERED_COPPER)
            .add(Material.OXIDIZED_COPPER)
            .add(Material.CUT_COPPER)
            .add(Material.EXPOSED_CUT_COPPER)
            .add(Material.WEATHERED_CUT_COPPER)
            .add(Material.OXIDIZED_CUT_COPPER)
            .add(Material.CUT_COPPER_STAIRS)
            .add(Material.EXPOSED_CUT_COPPER_STAIRS)
            .add(Material.WEATHERED_CUT_COPPER_STAIRS)
            .add(Material.OXIDIZED_CUT_COPPER_STAIRS)
            .add(Material.CUT_COPPER_SLAB)
            .add(Material.EXPOSED_CUT_COPPER_SLAB)
            .add(Material.WEATHERED_CUT_COPPER_SLAB)
            .add(Material.OXIDIZED_CUT_COPPER_SLAB)
            .add(Material.WAXED_COPPER_BLOCK)
            .add(Material.WAXED_EXPOSED_COPPER)
            .add(Material.WAXED_WEATHERED_COPPER)
            .add(Material.WAXED_OXIDIZED_COPPER)
            .add(Material.WAXED_CUT_COPPER)
            .add(Material.WAXED_EXPOSED_CUT_COPPER)
            .add(Material.WAXED_WEATHERED_CUT_COPPER)
            .add(Material.WAXED_OXIDIZED_CUT_COPPER)
            .add(Material.WAXED_CUT_COPPER_STAIRS)
            .add(Material.WAXED_EXPOSED_CUT_COPPER_STAIRS)
            .add(Material.WAXED_WEATHERED_CUT_COPPER_STAIRS)
            .add(Material.WAXED_OXIDIZED_CUT_COPPER_STAIRS)
            .add(Material.WAXED_CUT_COPPER_SLAB)
            .add(Material.WAXED_EXPOSED_CUT_COPPER_SLAB)
            .add(Material.WAXED_WEATHERED_CUT_COPPER_SLAB)
            .add(Material.WAXED_OXIDIZED_CUT_COPPER_SLAB)
            .build());

    /**
     * This is referring to materials that can exist in the world as blocks.
     */
    public static final Tag<Material> LIQUID_BLOCKS = new BetterTag<>("liquid_blocks",
        ImmutableSet.<Material>builder()
            .add(Material.LAVA)
            .add(Material.WATER)
            .add(Material.POWDER_SNOW)
            .build());

    // ------------------------------------------------------------
    // Better Tag class to allow for easy Tag creation.
    // ------------------------------------------------------------

    private static class BetterTag<T extends Keyed> implements Tag<T> {

        private final NamespacedKey key;

        private final Set<T> values;

        private BetterTag(final String key, final Set<T> values) {
            this.key = new NamespacedKey("civmodcore", key);
            this.values = values;
        }

        @Override
        public boolean isTagged(@Nullable T value) {
            return this.values.contains(value);
        }

        @Nonnull
        @Override
        public Set<T> getValues() {
            return this.values;
        }

        @Nonnull
        @Override
        public NamespacedKey getKey() {
            return this.key;
        }

    }

    // ------------------------------------------------------------
    // Initialise and check MoreTags
    // ------------------------------------------------------------

    public static void init() {
        final var logger = CivLogger.getLogger(MoreTags.class);
        // Determine if there's any crops missing
        {
            final Set<Material> missing = new HashSet<>();
            CollectionUtils.addAll(missing, Material.values());
            CollectionUtils.filter(missing, Material::isBlock); // Do this first to reduce amount of block data created
            CollectionUtils.filter(missing, material -> Bukkit.createBlockData(material) instanceof Ageable);
            missing.removeIf(Tag.ICE::isTagged);
            missing.removeIf(Tag.FIRE::isTagged);
            missing.removeIf(Tag.SAPLINGS::isTagged);
            missing.removeIf(CROPS::isTagged);
            if (!missing.isEmpty()) {
                logger.warning("The following crops are missing: " +
                    missing.stream().map(Material::name).collect(Collectors.joining(",")) + ".");
            }
        }
    }

}
