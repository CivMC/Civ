package net.civmc.kitpvp.kit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;

public enum KitItem {
    LEATHER_HELMET(Material.LEATHER_HELMET, KitCategory.ARMOUR, 0),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, KitCategory.ARMOUR, 0),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, KitCategory.ARMOUR, 0),
    LEATHER_BOOTS(Material.LEATHER_BOOTS, KitCategory.ARMOUR, 0),

    GOLDEN_HELMET(Material.GOLDEN_HELMET, KitCategory.ARMOUR, 0),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, KitCategory.ARMOUR, 0),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, KitCategory.ARMOUR, 0),
    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, KitCategory.ARMOUR, 0),

    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, KitCategory.ARMOUR, 0),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, KitCategory.ARMOUR, 0),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, KitCategory.ARMOUR, 0),
    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, KitCategory.ARMOUR, 0),

    IRON_HELMET(Material.IRON_HELMET, KitCategory.ARMOUR, 0),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, KitCategory.ARMOUR, 0),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, KitCategory.ARMOUR, 0),
    IRON_BOOTS(Material.IRON_BOOTS, KitCategory.ARMOUR, 0),

    DIAMOND_HELMET(Material.DIAMOND_HELMET, KitCategory.ARMOUR, 1),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, KitCategory.ARMOUR, 1),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, KitCategory.ARMOUR, 1),
    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, KitCategory.ARMOUR, 1),

    NETHERITE_HELMET(Material.NETHERITE_HELMET, KitCategory.ARMOUR, 7),
    NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, KitCategory.ARMOUR, 5),
    NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, KitCategory.ARMOUR, 5),
    NETHERITE_BOOTS(Material.NETHERITE_BOOTS, KitCategory.ARMOUR, 7),

    OBSIDIAN(Material.OBSIDIAN, KitCategory.BLOCK, 3),
    COBBLESTONE(Material.COBBLESTONE, KitCategory.BLOCK, 0),
    COBWEB(Material.COBWEB, KitCategory.BLOCK, 6),
    IRON_DOOR(Material.IRON_DOOR, KitCategory.BLOCK, 0),
    OAK_DOOR(Material.OAK_DOOR, KitCategory.BLOCK, 0),
    OAK_PLANKS(Material.OAK_PLANKS, KitCategory.BLOCK, 0),
    OAK_LOG(Material.OAK_LOG, KitCategory.BLOCK, 0),
    CRAFTING_TABLE(Material.CRAFTING_TABLE, KitCategory.BLOCK, 0),
    WHITE_WOOL(Material.WHITE_WOOL, KitCategory.BLOCK, 0),
    BONE_BLOCK(Material.BONE_BLOCK, KitCategory.BLOCK, 0),
    SANDSTONE(Material.SANDSTONE, KitCategory.BLOCK, 0),
    GRAVEL(Material.GRAVEL, KitCategory.BLOCK, 0),
    ICE(Material.ICE, KitCategory.BLOCK, 0),
    PACKED_ICE(Material.PACKED_ICE, KitCategory.BLOCK, 0),
    BLUE_ICE(Material.BLUE_ICE, KitCategory.BLOCK, 0),
    OAK_TRAPDOOR(Material.OAK_TRAPDOOR, KitCategory.BLOCK, 0),
    HAY_BLOCK(Material.HAY_BLOCK, KitCategory.BLOCK, 0),
    SOUL_SAND(Material.SOUL_SAND, KitCategory.BLOCK, 0),
    SOUL_SOIL(Material.SOUL_SOIL, KitCategory.BLOCK, 0),
    DIRT(Material.DIRT, KitCategory.BLOCK, 0),
    SAND(Material.SAND, KitCategory.BLOCK, 0),
    STONE(Material.STONE, KitCategory.BLOCK, 0),
    TNT(Material.TNT, KitCategory.BLOCK, 10),
    SCAFFOLDING(Material.SCAFFOLDING, KitCategory.BLOCK, 0),
    SLIME_BLOCK(Material.SLIME_BLOCK, KitCategory.BLOCK, 0),
    HONEY_BLOCK(Material.HONEY_BLOCK, KitCategory.BLOCK, 0),
    GLOWSTONE(Material.GLOWSTONE, KitCategory.BLOCK, 0),
    GLASS(Material.GLASS, KitCategory.BLOCK, 2),
    NETHERRACK(Material.NETHERRACK, KitCategory.BLOCK, 0),
    RAIL(Material.RAIL, KitCategory.BLOCK, 0),
    IRON_BLOCK(Material.IRON_BLOCK, KitCategory.BLOCK, 0),
    GOLD_BLOCK(Material.GOLD_BLOCK, KitCategory.BLOCK, 0),
    BEACON(Material.BEACON, KitCategory.BLOCK, 10),
    COBBLED_DEEPSLATE(Material.COBBLED_DEEPSLATE, KitCategory.BLOCK, 0),
    LAPIS_BLOCK(Material.LAPIS_BLOCK, KitCategory.BLOCK, 0),
    REDSTONE_BLOCK(Material.REDSTONE_BLOCK, KitCategory.BLOCK, 0),
    COPPER_BLOCK(Material.COPPER_BLOCK, KitCategory.BLOCK, 0),
    BIRCH_PLANKS(Material.BIRCH_PLANKS, KitCategory.BLOCK, 0),
    DARK_OAK_PLANKS(Material.DARK_OAK_PLANKS, KitCategory.BLOCK, 0),

    WOODEN_SWORD(Material.WOODEN_SWORD, KitCategory.TOOL, 0),
    WOODEN_AXE(Material.WOODEN_AXE, KitCategory.TOOL, 0),
    WOODEN_PICKAXE(Material.WOODEN_PICKAXE, KitCategory.TOOL, 0),
    WOODEN_SHOVEL(Material.WOODEN_SHOVEL, KitCategory.TOOL, 0),
    WOODEN_HOE(Material.WOODEN_HOE, KitCategory.TOOL, 0),

    GOLDEN_SWORD(Material.GOLDEN_SWORD, KitCategory.TOOL, 0),
    GOLDEN_AXE(Material.GOLDEN_AXE, KitCategory.TOOL, 0),
    GOLDEN_PICKAXE(Material.GOLDEN_PICKAXE, KitCategory.TOOL, 0),
    GOLDEN_SHOVEL(Material.GOLDEN_SHOVEL, KitCategory.TOOL, 0),
    GOLDEN_HOE(Material.GOLDEN_HOE, KitCategory.TOOL, 0),

    STONE_SWORD(Material.STONE_SWORD, KitCategory.TOOL, 0),
    STONE_AXE(Material.STONE_AXE, KitCategory.TOOL, 0),
    STONE_PICKAXE(Material.STONE_PICKAXE, KitCategory.TOOL, 0),
    STONE_SHOVEL(Material.STONE_SHOVEL, KitCategory.TOOL, 0),
    STONE_HOE(Material.STONE_HOE, KitCategory.TOOL, 0),

    IRON_SWORD(Material.IRON_SWORD, KitCategory.TOOL, 0),
    IRON_AXE(Material.IRON_AXE, KitCategory.TOOL, 0),
    IRON_PICKAXE(Material.IRON_PICKAXE, KitCategory.TOOL, 0),
    IRON_SHOVEL(Material.IRON_SHOVEL, KitCategory.TOOL, 0),
    IRON_HOE(Material.IRON_HOE, KitCategory.TOOL, 0),

    DIAMOND_SWORD(Material.DIAMOND_SWORD, KitCategory.TOOL, 1),
    DIAMOND_AXE(Material.DIAMOND_AXE, KitCategory.TOOL, 1),
    DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, KitCategory.TOOL, 1),
    DIAMOND_SHOVEL(Material.DIAMOND_SHOVEL, KitCategory.TOOL, 1),
    DIAMOND_HOE(Material.DIAMOND_HOE, KitCategory.TOOL, 1),

    NETHERITE_SWORD(Material.NETHERITE_SWORD, KitCategory.TOOL, 5),
    NETHERITE_AXE(Material.NETHERITE_AXE, KitCategory.TOOL, 6),
    NETHERITE_PICKAXE(Material.NETHERITE_PICKAXE, KitCategory.TOOL, 5),
    NETHERITE_SHOVEL(Material.NETHERITE_SHOVEL, KitCategory.TOOL, 5),
    NETHERITE_HOE(Material.NETHERITE_HOE, KitCategory.TOOL, 5),

    SHIELD(Material.SHIELD, KitCategory.TOOL, 0),
    TRIDENT(Material.TRIDENT, KitCategory.TOOL, 4),
    FLINT_AND_STEEL(Material.FLINT_AND_STEEL, KitCategory.TOOL, 0),

    SHEARS(Material.SHEARS, KitCategory.TOOL, 0),
    ENDER_PEARL(Material.ENDER_PEARL, KitCategory.TOOL, 2),
    FIREWORK_ROCKET(Material.FIREWORK_ROCKET, KitCategory.TOOL, 1),

    BUCKET(Material.BUCKET, KitCategory.TOOL, 0),
    WATER_BUCKET(Material.WATER_BUCKET, KitCategory.TOOL, 0),
    LAVA_BUCKET(Material.LAVA_BUCKET, KitCategory.TOOL, 0),

    MILK_BUCKET(Material.MILK_BUCKET, KitCategory.TOOL, 0),
    POWDER_SNOW_BUCKET(Material.POWDER_SNOW_BUCKET, KitCategory.TOOL, 0),
    ARROW(Material.ARROW, KitCategory.TOOL, 0),

    BOW(Material.BOW, KitCategory.TOOL, 1),
    CROSSBOW(Material.CROSSBOW, KitCategory.TOOL, 4),
    FISHING_ROD(Material.FISHING_ROD, KitCategory.TOOL, 1),
    OAK_BOAT(Material.OAK_BOAT, KitCategory.TOOL, 1),
    TNT_MINECART(Material.TNT_MINECART, KitCategory.TOOL, 3),

    ELYTRA(Material.ELYTRA, KitCategory.ARMOUR, 100),
    TURTLE_HELMET(Material.TURTLE_HELMET, KitCategory.ARMOUR, 2),

    COOKED_PORKCHOP(Material.COOKED_PORKCHOP, KitCategory.FOOD, 0),
    GOLDEN_APPLE(Material.GOLDEN_APPLE, KitCategory.FOOD, 5),
    BAKED_POTATO(Material.BAKED_POTATO, KitCategory.FOOD, 0),
    COOKED_BEEF(Material.COOKED_BEEF, KitCategory.FOOD, 0),
    GOLDEN_CARROT(Material.GOLDEN_CARROT, KitCategory.FOOD, 0),
    SWEET_BERRIES(Material.SWEET_BERRIES, KitCategory.FOOD, 0),
    PUMPKIN_PIE(Material.PUMPKIN_PIE, KitCategory.FOOD, 0),
    BREAD(Material.BREAD, KitCategory.FOOD, 0),
    COOKED_CHICKEN(Material.COOKED_CHICKEN, KitCategory.FOOD, 0),
    CHORUS_FRUIT(Material.CHORUS_FRUIT, KitCategory.FOOD, 10),
    ;

    private static final Map<KitCategory, List<KitItem>> itemsByCategory = new EnumMap<>(KitCategory.class);

    static {
        for (KitItem item : values()) {
            itemsByCategory.computeIfAbsent(item.category, k -> new ArrayList<>()).add(item);
        }
    }

    private final Material item;
    private final KitCategory category;
    private final int cost;

    KitItem(Material item, KitCategory category, int cost) {
        this.item = item;
        this.category = category;
        this.cost = cost;
    }

    public Material getItem() {
        return item;
    }

    public int getCost() {
        return cost;
    }

    public static List<KitItem> getItems(KitCategory category) {
        return Collections.unmodifiableList(itemsByCategory.get(category));
    }
}
