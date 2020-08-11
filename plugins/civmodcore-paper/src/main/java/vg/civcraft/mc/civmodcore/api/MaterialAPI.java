package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.Tag;

/**
 * Class of static APIs for Materials, filling in the gaps left by {@link org.bukkit.Tag Tag}.
 *
 * Tag.ACACIA_LOGS:-
 *  - Material.ACACIA_LOG
 *  - Material.ACACIA_WOOD
 *  - Material.STRIPPED_ACACIA_LOG
 *  - Material.STRIPPED_ACACIA_WOOD
 *
 * Tag.ANVIL:-
 *  - Material.ANVIL
 *  - Material.CHIPPED_ANVIL
 *  - Material.DAMAGED_ANVIL
 *
 * Tag.BAMBOO_PLANTABLE_ON:-
 *  - Material.BAMBOO
 *  - Material.BAMBOO_SAPLING
 *  - Material.COARSE_DIRT
 *  - Material.DIRT
 *  - Material.GRASS_BLOCK
 *  - Material.GRAVEL
 *  - Material.MYCELIUM
 *  - Material.PODZOL
 *  - Material.RED_SAND
 *  - Material.SAND
 *
 * Tag.BANNERS:-
 *  - Material.BLACK_BANNER
 *  - Material.BLACK_WALL_BANNER
 *  - Material.BLUE_BANNER
 *  - Material.BLUE_WALL_BANNER
 *  - Material.BROWN_BANNER
 *  - Material.BROWN_WALL_BANNER
 *  - Material.CYAN_BANNER
 *  - Material.CYAN_WALL_BANNER
 *  - Material.GRAY_BANNER
 *  - Material.GRAY_WALL_BANNER
 *  - Material.GREEN_BANNER
 *  - Material.GREEN_WALL_BANNER
 *  - Material.LIGHT_BLUE_BANNER
 *  - Material.LIGHT_BLUE_WALL_BANNER
 *  - Material.LIGHT_GRAY_BANNER
 *  - Material.LIGHT_GRAY_WALL_BANNER
 *  - Material.LIME_BANNER
 *  - Material.LIME_WALL_BANNER
 *  - Material.MAGENTA_BANNER
 *  - Material.MAGENTA_WALL_BANNER
 *  - Material.ORANGE_BANNER
 *  - Material.ORANGE_WALL_BANNER
 *  - Material.PINK_BANNER
 *  - Material.PINK_WALL_BANNER
 *  - Material.PURPLE_BANNER
 *  - Material.PURPLE_WALL_BANNER
 *  - Material.RED_BANNER
 *  - Material.RED_WALL_BANNER
 *  - Material.WHITE_BANNER
 *  - Material.WHITE_WALL_BANNER
 *  - Material.YELLOW_BANNER
 *  - Material.YELLOW_WALL_BANNER
 *
 * Tag.BEDS:-
 *  - Material.BLACK_BED
 *  - Material.BLUE_BED
 *  - Material.BROWN_BED
 *  - Material.CYAN_BED
 *  - Material.GRAY_BED
 *  - Material.GREEN_BED
 *  - Material.LIGHT_BLUE_BED
 *  - Material.LIGHT_GRAY_BED
 *  - Material.LIME_BED
 *  - Material.MAGENTA_BED
 *  - Material.ORANGE_BED
 *  - Material.PINK_BED
 *  - Material.PURPLE_BED
 *  - Material.RED_BED
 *  - Material.WHITE_BED
 *  - Material.YELLOW_BED
 *
 * Tag.BIRCH_LOGS:-
 *  - Material.BIRCH_LOG
 *  - Material.BIRCH_WOOD
 *  - Material.STRIPPED_BIRCH_LOG
 *  - Material.STRIPPED_BIRCH_WOOD
 *
 * Tag.BUTTONS:-
 *  - Material.ACACIA_BUTTON
 *  - Material.BIRCH_BUTTON
 *  - Material.DARK_OAK_BUTTON
 *  - Material.JUNGLE_BUTTON
 *  - Material.OAK_BUTTON
 *  - Material.SPRUCE_BUTTON
 *  - Material.STONE_BUTTON
 *
 * Tag.CARPETS:-
 *  - Material.BLACK_CARPET
 *  - Material.BLUE_CARPET
 *  - Material.BROWN_CARPET
 *  - Material.CYAN_CARPET
 *  - Material.GRAY_CARPET
 *  - Material.GREEN_CARPET
 *  - Material.LIGHT_BLUE_CARPET
 *  - Material.LIGHT_GRAY_CARPET
 *  - Material.LIME_CARPET
 *  - Material.MAGENTA_CARPET
 *  - Material.ORANGE_CARPET
 *  - Material.PINK_CARPET
 *  - Material.PURPLE_CARPET
 *  - Material.RED_CARPET
 *  - Material.WHITE_CARPET
 *  - Material.YELLOW_CARPET
 *
 * Tag.CORALS:-
 *  - Material.BRAIN_CORAL
 *  - Material.BRAIN_CORAL_FAN
 *  - Material.BUBBLE_CORAL
 *  - Material.BUBBLE_CORAL_FAN
 *  - Material.FIRE_CORAL
 *  - Material.FIRE_CORAL_FAN
 *  - Material.HORN_CORAL
 *  - Material.HORN_CORAL_FAN
 *  - Material.TUBE_CORAL
 *  - Material.TUBE_CORAL_FAN
 *
 * Tag.CORAL_BLOCKS:-
 *  - Material.BRAIN_CORAL_BLOCK
 *  - Material.BUBBLE_CORAL_BLOCK
 *  - Material.FIRE_CORAL_BLOCK
 *  - Material.HORN_CORAL_BLOCK
 *  - Material.TUBE_CORAL_BLOCK
 *
 * Tag.CORAL_PLANTS:-
 *  - Material.BRAIN_CORAL
 *  - Material.BUBBLE_CORAL
 *  - Material.FIRE_CORAL
 *  - Material.HORN_CORAL
 *  - Material.TUBE_CORAL
 *
 * Tag.DARK_OAK_LOGS:-
 *  - Material.DARK_OAK_LOG
 *  - Material.DARK_OAK_WOOD
 *  - Material.STRIPPED_DARK_OAK_LOG
 *  - Material.STRIPPED_DARK_OAK_WOOD
 *
 * Tag.DIRT_LIKE:-
 *  - Material.COARSE_DIRT
 *  - Material.DIRT
 *  - Material.GRASS_BLOCK
 *  - Material.MYCELIUM
 *  - Material.PODZOL
 *
 * Tag.DOORS:-
 *  - Material.ACACIA_DOOR
 *  - Material.BIRCH_DOOR
 *  - Material.DARK_OAK_DOOR
 *  - Material.IRON_DOOR
 *  - Material.JUNGLE_DOOR
 *  - Material.OAK_DOOR
 *  - Material.SPRUCE_DOOR
 *
 * Tag.ENDERMAN_HOLDABLE:-
 *  - Material.ALLIUM
 *  - Material.AZURE_BLUET
 *  - Material.BLUE_ORCHID
 *  - Material.BROWN_MUSHROOM
 *  - Material.CACTUS
 *  - Material.CARVED_PUMPKIN
 *  - Material.CLAY
 *  - Material.COARSE_DIRT
 *  - Material.CORNFLOWER
 *  - Material.DANDELION
 *  - Material.DIRT
 *  - Material.GRASS_BLOCK
 *  - Material.GRAVEL
 *  - Material.LILY_OF_THE_VALLEY
 *  - Material.MELON
 *  - Material.MYCELIUM
 *  - Material.NETHERRACK
 *  - Material.ORANGE_TULIP
 *  - Material.OXEYE_DAISY
 *  - Material.PINK_TULIP
 *  - Material.PODZOL
 *  - Material.POPPY
 *  - Material.PUMPKIN
 *  - Material.RED_MUSHROOM
 *  - Material.RED_SAND
 *  - Material.RED_TULIP
 *  - Material.SAND
 *  - Material.TNT
 *  - Material.WHITE_TULIP
 *  - Material.WITHER_ROSE
 *
 * Tag.FENCES:-
 *  - Material.ACACIA_FENCE
 *  - Material.BIRCH_FENCE
 *  - Material.DARK_OAK_FENCE
 *  - Material.JUNGLE_FENCE
 *  - Material.NETHER_BRICK_FENCE
 *  - Material.OAK_FENCE
 *  - Material.SPRUCE_FENCE
 *
 * Tag.FLOWER_POTS:-
 *  - Material.FLOWER_POT
 *  - Material.POTTED_ACACIA_SAPLING
 *  - Material.POTTED_ALLIUM
 *  - Material.POTTED_AZURE_BLUET
 *  - Material.POTTED_BAMBOO
 *  - Material.POTTED_BIRCH_SAPLING
 *  - Material.POTTED_BLUE_ORCHID
 *  - Material.POTTED_BROWN_MUSHROOM
 *  - Material.POTTED_CACTUS
 *  - Material.POTTED_CORNFLOWER
 *  - Material.POTTED_DANDELION
 *  - Material.POTTED_DARK_OAK_SAPLING
 *  - Material.POTTED_DEAD_BUSH
 *  - Material.POTTED_FERN
 *  - Material.POTTED_JUNGLE_SAPLING
 *  - Material.POTTED_LILY_OF_THE_VALLEY
 *  - Material.POTTED_OAK_SAPLING
 *  - Material.POTTED_ORANGE_TULIP
 *  - Material.POTTED_OXEYE_DAISY
 *  - Material.POTTED_PINK_TULIP
 *  - Material.POTTED_POPPY
 *  - Material.POTTED_RED_MUSHROOM
 *  - Material.POTTED_RED_TULIP
 *  - Material.POTTED_SPRUCE_SAPLING
 *  - Material.POTTED_WHITE_TULIP
 *  - Material.POTTED_WITHER_ROSE
 *
 * Tag.ICE:-
 *  - Material.BLUE_ICE
 *  - Material.FROSTED_ICE
 *  - Material.ICE
 *  - Material.PACKED_ICE
 *
 * Tag.IMPERMEABLE:-
 *  - Material.BLACK_STAINED_GLASS
 *  - Material.BLUE_STAINED_GLASS
 *  - Material.BROWN_STAINED_GLASS
 *  - Material.CYAN_STAINED_GLASS
 *  - Material.GLASS
 *  - Material.GRAY_STAINED_GLASS
 *  - Material.GREEN_STAINED_GLASS
 *  - Material.LIGHT_BLUE_STAINED_GLASS
 *  - Material.LIGHT_GRAY_STAINED_GLASS
 *  - Material.LIME_STAINED_GLASS
 *  - Material.MAGENTA_STAINED_GLASS
 *  - Material.ORANGE_STAINED_GLASS
 *  - Material.PINK_STAINED_GLASS
 *  - Material.PURPLE_STAINED_GLASS
 *  - Material.RED_STAINED_GLASS
 *  - Material.WHITE_STAINED_GLASS
 *  - Material.YELLOW_STAINED_GLASS
 *
 * Tag.ITEMS_ARROWS:-
 *  - Material.ARROW
 *  - Material.SPECTRAL_ARROW
 *  - Material.TIPPED_ARROW
 *
 * Tag.ITEMS_BANNERS:-
 *  - Material.BLACK_BANNER
 *  - Material.BLUE_BANNER
 *  - Material.BROWN_BANNER
 *  - Material.CYAN_BANNER
 *  - Material.GRAY_BANNER
 *  - Material.GREEN_BANNER
 *  - Material.LIGHT_BLUE_BANNER
 *  - Material.LIGHT_GRAY_BANNER
 *  - Material.LIME_BANNER
 *  - Material.MAGENTA_BANNER
 *  - Material.ORANGE_BANNER
 *  - Material.PINK_BANNER
 *  - Material.PURPLE_BANNER
 *  - Material.RED_BANNER
 *  - Material.WHITE_BANNER
 *  - Material.YELLOW_BANNER
 *
 * Tag.ITEMS_BOATS:-
 *  - Material.ACACIA_BOAT
 *  - Material.BIRCH_BOAT
 *  - Material.DARK_OAK_BOAT
 *  - Material.JUNGLE_BOAT
 *  - Material.OAK_BOAT
 *  - Material.SPRUCE_BOAT
 *
 * Tag.ITEMS_COALS:-
 *  - Material.CHARCOAL
 *  - Material.COAL
 *
 * Tag.ITEMS_FISHES:-
 *  - Material.COD
 *  - Material.COOKED_COD
 *  - Material.COOKED_SALMON
 *  - Material.PUFFERFISH
 *  - Material.SALMON
 *  - Material.TROPICAL_FISH
 *
 * Tag.ITEMS_MUSIC_DISCS:-
 *  - Material.MUSIC_DISC_11
 *  - Material.MUSIC_DISC_13
 *  - Material.MUSIC_DISC_BLOCKS
 *  - Material.MUSIC_DISC_CAT
 *  - Material.MUSIC_DISC_CHIRP
 *  - Material.MUSIC_DISC_FAR
 *  - Material.MUSIC_DISC_MALL
 *  - Material.MUSIC_DISC_MELLOHI
 *  - Material.MUSIC_DISC_STAL
 *  - Material.MUSIC_DISC_STRAD
 *  - Material.MUSIC_DISC_WAIT
 *  - Material.MUSIC_DISC_WARD
 *
 * Tag.JUNGLE_LOGS:-
 *  - Material.JUNGLE_LOG
 *  - Material.JUNGLE_WOOD
 *  - Material.STRIPPED_JUNGLE_LOG
 *  - Material.STRIPPED_JUNGLE_WOOD
 *
 * Tag.LEAVES:-
 *  - Material.ACACIA_LEAVES
 *  - Material.BIRCH_LEAVES
 *  - Material.DARK_OAK_LEAVES
 *  - Material.JUNGLE_LEAVES
 *  - Material.OAK_LEAVES
 *  - Material.SPRUCE_LEAVES
 *
 * Tag.LOGS:-
 *  - Material.ACACIA_LOG
 *  - Material.ACACIA_WOOD
 *  - Material.BIRCH_LOG
 *  - Material.BIRCH_WOOD
 *  - Material.DARK_OAK_LOG
 *  - Material.DARK_OAK_WOOD
 *  - Material.JUNGLE_LOG
 *  - Material.JUNGLE_WOOD
 *  - Material.OAK_LOG
 *  - Material.OAK_WOOD
 *  - Material.SPRUCE_LOG
 *  - Material.SPRUCE_WOOD
 *  - Material.STRIPPED_ACACIA_LOG
 *  - Material.STRIPPED_ACACIA_WOOD
 *  - Material.STRIPPED_BIRCH_LOG
 *  - Material.STRIPPED_BIRCH_WOOD
 *  - Material.STRIPPED_DARK_OAK_LOG
 *  - Material.STRIPPED_DARK_OAK_WOOD
 *  - Material.STRIPPED_JUNGLE_LOG
 *  - Material.STRIPPED_JUNGLE_WOOD
 *  - Material.STRIPPED_OAK_LOG
 *  - Material.STRIPPED_OAK_WOOD
 *  - Material.STRIPPED_SPRUCE_LOG
 *  - Material.STRIPPED_SPRUCE_WOOD
 *
 * Tag.OAK_LOGS:-
 *  - Material.OAK_LOG
 *  - Material.OAK_WOOD
 *  - Material.STRIPPED_OAK_LOG
 *  - Material.STRIPPED_OAK_WOOD
 *
 * Tag.PLANKS:-
 *  - Material.ACACIA_PLANKS
 *  - Material.BIRCH_PLANKS
 *  - Material.DARK_OAK_PLANKS
 *  - Material.JUNGLE_PLANKS
 *  - Material.OAK_PLANKS
 *  - Material.SPRUCE_PLANKS
 *
 * Tag.RAILS:-
 *  - Material.ACTIVATOR_RAIL
 *  - Material.DETECTOR_RAIL
 *  - Material.POWERED_RAIL
 *  - Material.RAIL
 *
 * Tag.SAND:-
 *  - Material.RED_SAND
 *  - Material.SAND
 *
 * Tag.SAPLINGS:-
 *  - Material.ACACIA_SAPLING
 *  - Material.BIRCH_SAPLING
 *  - Material.DARK_OAK_SAPLING
 *  - Material.JUNGLE_SAPLING
 *  - Material.OAK_SAPLING
 *  - Material.SPRUCE_SAPLING
 *
 * Tag.SIGNS:-
 *  - Material.ACACIA_SIGN
 *  - Material.ACACIA_WALL_SIGN
 *  - Material.BIRCH_SIGN
 *  - Material.BIRCH_WALL_SIGN
 *  - Material.DARK_OAK_SIGN
 *  - Material.DARK_OAK_WALL_SIGN
 *  - Material.JUNGLE_SIGN
 *  - Material.JUNGLE_WALL_SIGN
 *  - Material.OAK_SIGN
 *  - Material.OAK_WALL_SIGN
 *  - Material.SPRUCE_SIGN
 *  - Material.SPRUCE_WALL_SIGN
 *
 * Tag.SLABS:-
 *  - Material.ACACIA_SLAB
 *  - Material.ANDESITE_SLAB
 *  - Material.BIRCH_SLAB
 *  - Material.BRICK_SLAB
 *  - Material.COBBLESTONE_SLAB
 *  - Material.CUT_RED_SANDSTONE_SLAB
 *  - Material.CUT_SANDSTONE_SLAB
 *  - Material.DARK_OAK_SLAB
 *  - Material.DARK_PRISMARINE_SLAB
 *  - Material.DIORITE_SLAB
 *  - Material.END_STONE_BRICK_SLAB
 *  - Material.GRANITE_SLAB
 *  - Material.JUNGLE_SLAB
 *  - Material.MOSSY_COBBLESTONE_SLAB
 *  - Material.MOSSY_STONE_BRICK_SLAB
 *  - Material.NETHER_BRICK_SLAB
 *  - Material.OAK_SLAB
 *  - Material.PETRIFIED_OAK_SLAB
 *  - Material.POLISHED_ANDESITE_SLAB
 *  - Material.POLISHED_DIORITE_SLAB
 *  - Material.POLISHED_GRANITE_SLAB
 *  - Material.PRISMARINE_BRICK_SLAB
 *  - Material.PRISMARINE_SLAB
 *  - Material.PURPUR_SLAB
 *  - Material.QUARTZ_SLAB
 *  - Material.RED_NETHER_BRICK_SLAB
 *  - Material.RED_SANDSTONE_SLAB
 *  - Material.SANDSTONE_SLAB
 *  - Material.SMOOTH_QUARTZ_SLAB
 *  - Material.SMOOTH_RED_SANDSTONE_SLAB
 *  - Material.SMOOTH_SANDSTONE_SLAB
 *  - Material.SMOOTH_STONE_SLAB
 *  - Material.SPRUCE_SLAB
 *  - Material.STONE_BRICK_SLAB
 *  - Material.STONE_SLAB
 *
 * Tag.SMALL_FLOWERS:-
 *  - Material.ALLIUM
 *  - Material.AZURE_BLUET
 *  - Material.BLUE_ORCHID
 *  - Material.CORNFLOWER
 *  - Material.DANDELION
 *  - Material.LILY_OF_THE_VALLEY
 *  - Material.ORANGE_TULIP
 *  - Material.OXEYE_DAISY
 *  - Material.PINK_TULIP
 *  - Material.POPPY
 *  - Material.RED_TULIP
 *  - Material.WHITE_TULIP
 *  - Material.WITHER_ROSE
 *
 * Tag.SPRUCE_LOGS:-
 *  - Material.SPRUCE_LOG
 *  - Material.SPRUCE_WOOD
 *  - Material.STRIPPED_SPRUCE_LOG
 *  - Material.STRIPPED_SPRUCE_WOOD
 *
 * Tag.STAIRS:-
 *  - Material.ACACIA_STAIRS
 *  - Material.ANDESITE_STAIRS
 *  - Material.BIRCH_STAIRS
 *  - Material.BRICK_STAIRS
 *  - Material.COBBLESTONE_STAIRS
 *  - Material.DARK_OAK_STAIRS
 *  - Material.DARK_PRISMARINE_STAIRS
 *  - Material.DIORITE_STAIRS
 *  - Material.END_STONE_BRICK_STAIRS
 *  - Material.GRANITE_STAIRS
 *  - Material.JUNGLE_STAIRS
 *  - Material.MOSSY_COBBLESTONE_STAIRS
 *  - Material.MOSSY_STONE_BRICK_STAIRS
 *  - Material.NETHER_BRICK_STAIRS
 *  - Material.OAK_STAIRS
 *  - Material.POLISHED_ANDESITE_STAIRS
 *  - Material.POLISHED_DIORITE_STAIRS
 *  - Material.POLISHED_GRANITE_STAIRS
 *  - Material.PRISMARINE_BRICK_STAIRS
 *  - Material.PRISMARINE_STAIRS
 *  - Material.PURPUR_STAIRS
 *  - Material.QUARTZ_STAIRS
 *  - Material.RED_NETHER_BRICK_STAIRS
 *  - Material.RED_SANDSTONE_STAIRS
 *  - Material.SANDSTONE_STAIRS
 *  - Material.SMOOTH_QUARTZ_STAIRS
 *  - Material.SMOOTH_RED_SANDSTONE_STAIRS
 *  - Material.SMOOTH_SANDSTONE_STAIRS
 *  - Material.SPRUCE_STAIRS
 *  - Material.STONE_BRICK_STAIRS
 *  - Material.STONE_STAIRS
 *
 * Tag.STANDING_SIGNS:-
 *  - Material.ACACIA_SIGN
 *  - Material.BIRCH_SIGN
 *  - Material.DARK_OAK_SIGN
 *  - Material.JUNGLE_SIGN
 *  - Material.OAK_SIGN
 *  - Material.SPRUCE_SIGN
 *
 * Tag.STONE_BRICKS:-
 *  - Material.CHISELED_STONE_BRICKS
 *  - Material.CRACKED_STONE_BRICKS
 *  - Material.MOSSY_STONE_BRICKS
 *  - Material.STONE_BRICKS
 *
 * Tag.TRAPDOORS:-
 *  - Material.ACACIA_TRAPDOOR
 *  - Material.BIRCH_TRAPDOOR
 *  - Material.DARK_OAK_TRAPDOOR
 *  - Material.IRON_TRAPDOOR
 *  - Material.JUNGLE_TRAPDOOR
 *  - Material.OAK_TRAPDOOR
 *  - Material.SPRUCE_TRAPDOOR
 *
 * Tag.UNDERWATER_BONEMEALS:-
 *  - Material.BRAIN_CORAL
 *  - Material.BRAIN_CORAL_FAN
 *  - Material.BRAIN_CORAL_WALL_FAN
 *  - Material.BUBBLE_CORAL
 *  - Material.BUBBLE_CORAL_FAN
 *  - Material.BUBBLE_CORAL_WALL_FAN
 *  - Material.FIRE_CORAL
 *  - Material.FIRE_CORAL_FAN
 *  - Material.FIRE_CORAL_WALL_FAN
 *  - Material.HORN_CORAL
 *  - Material.HORN_CORAL_FAN
 *  - Material.HORN_CORAL_WALL_FAN
 *  - Material.SEAGRASS
 *  - Material.TUBE_CORAL
 *  - Material.TUBE_CORAL_FAN
 *  - Material.TUBE_CORAL_WALL_FAN
 *
 * Tag.VALID_SPAWN:-
 *  - Material.GRASS_BLOCK
 *  - Material.PODZOL
 *
 * Tag.WALLS:-
 *  - Material.ANDESITE_WALL
 *  - Material.BRICK_WALL
 *  - Material.COBBLESTONE_WALL
 *  - Material.DIORITE_WALL
 *  - Material.END_STONE_BRICK_WALL
 *  - Material.GRANITE_WALL
 *  - Material.MOSSY_COBBLESTONE_WALL
 *  - Material.MOSSY_STONE_BRICK_WALL
 *  - Material.NETHER_BRICK_WALL
 *  - Material.PRISMARINE_WALL
 *  - Material.RED_NETHER_BRICK_WALL
 *  - Material.RED_SANDSTONE_WALL
 *  - Material.SANDSTONE_WALL
 *  - Material.STONE_BRICK_WALL
 *
 * Tag.WALL_CORALS:-
 *  - Material.BRAIN_CORAL_WALL_FAN
 *  - Material.BUBBLE_CORAL_WALL_FAN
 *  - Material.FIRE_CORAL_WALL_FAN
 *  - Material.HORN_CORAL_WALL_FAN
 *  - Material.TUBE_CORAL_WALL_FAN
 *
 * Tag.WALL_SIGNS:-
 *  - Material.ACACIA_WALL_SIGN
 *  - Material.BIRCH_WALL_SIGN
 *  - Material.DARK_OAK_WALL_SIGN
 *  - Material.JUNGLE_WALL_SIGN
 *  - Material.OAK_WALL_SIGN
 *  - Material.SPRUCE_WALL_SIGN
 *
 * Tag.WOODEN_BUTTONS:-
 *  - Material.ACACIA_BUTTON
 *  - Material.BIRCH_BUTTON
 *  - Material.DARK_OAK_BUTTON
 *  - Material.JUNGLE_BUTTON
 *  - Material.OAK_BUTTON
 *  - Material.SPRUCE_BUTTON
 *
 * Tag.WOODEN_DOORS:-
 *  - Material.ACACIA_DOOR
 *  - Material.BIRCH_DOOR
 *  - Material.DARK_OAK_DOOR
 *  - Material.JUNGLE_DOOR
 *  - Material.OAK_DOOR
 *  - Material.SPRUCE_DOOR
 *
 * Tag.WOODEN_FENCES:-
 *  - Material.ACACIA_FENCE
 *  - Material.BIRCH_FENCE
 *  - Material.DARK_OAK_FENCE
 *  - Material.JUNGLE_FENCE
 *  - Material.OAK_FENCE
 *  - Material.SPRUCE_FENCE
 *
 * Tag.WOODEN_PRESSURE_PLATES:-
 *  - Material.ACACIA_PRESSURE_PLATE
 *  - Material.BIRCH_PRESSURE_PLATE
 *  - Material.DARK_OAK_PRESSURE_PLATE
 *  - Material.JUNGLE_PRESSURE_PLATE
 *  - Material.OAK_PRESSURE_PLATE
 *  - Material.SPRUCE_PRESSURE_PLATE
 *
 * Tag.WOODEN_SLABS:-
 *  - Material.ACACIA_SLAB
 *  - Material.BIRCH_SLAB
 *  - Material.DARK_OAK_SLAB
 *  - Material.JUNGLE_SLAB
 *  - Material.OAK_SLAB
 *  - Material.SPRUCE_SLAB
 *
 * Tag.WOODEN_STAIRS:-
 *  - Material.ACACIA_STAIRS
 *  - Material.BIRCH_STAIRS
 *  - Material.DARK_OAK_STAIRS
 *  - Material.JUNGLE_STAIRS
 *  - Material.OAK_STAIRS
 *  - Material.SPRUCE_STAIRS
 *
 * Tag.WOODEN_TRAPDOORS:-
 *  - Material.ACACIA_TRAPDOOR
 *  - Material.BIRCH_TRAPDOOR
 *  - Material.DARK_OAK_TRAPDOOR
 *  - Material.JUNGLE_TRAPDOOR
 *  - Material.OAK_TRAPDOOR
 *  - Material.SPRUCE_TRAPDOOR
 *
 * Tag.WOOL:-
 *  - Material.BLACK_WOOL
 *  - Material.BLUE_WOOL
 *  - Material.BROWN_WOOL
 *  - Material.CYAN_WOOL
 *  - Material.GRAY_WOOL
 *  - Material.GREEN_WOOL
 *  - Material.LIGHT_BLUE_WOOL
 *  - Material.LIGHT_GRAY_WOOL
 *  - Material.LIME_WOOL
 *  - Material.MAGENTA_WOOL
 *  - Material.ORANGE_WOOL
 *  - Material.PINK_WOOL
 *  - Material.PURPLE_WOOL
 *  - Material.RED_WOOL
 *  - Material.WHITE_WOOL
 *  - Material.YELLOW_WOOL
 *
 * See also:
 * {@link SpawnEggAPI SpawnEggAPI}
 * {@link TreeTypeAPI TreeTypeAPI}
 */
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
	 */
	public static boolean isValidItemMaterial(Material material) {
		if (material == null) {
			return false;
		}
		if (material == Material.AIR) {
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
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
	 */
	public static Material getMaterialHash(Object object) {
		if (object == null) {
			return hashMaterials.get(0);
		}
		int index = IntMath.mod(object.hashCode(), hashMaterials.size());
		return hashMaterials.get(index);
	}

}
