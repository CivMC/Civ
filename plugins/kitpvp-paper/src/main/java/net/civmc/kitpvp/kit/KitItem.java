package net.civmc.kitpvp.kit;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public enum KitItem {
    LEATHER_HELMET(Material.LEATHER_HELMET, KitCategory.ARMOUR, 1),
    LEATHER_CHESTPLATE(Material.LEATHER_CHESTPLATE, KitCategory.ARMOUR, 1),
    LEATHER_LEGGINGS(Material.LEATHER_LEGGINGS, KitCategory.ARMOUR, 1),
    LEATHER_BOOTS(Material.LEATHER_BOOTS, KitCategory.ARMOUR, 1),

    GOLDEN_HELMET(Material.GOLDEN_HELMET, KitCategory.ARMOUR, 2),
    GOLDEN_CHESTPLATE(Material.GOLDEN_CHESTPLATE, KitCategory.ARMOUR, 2),
    GOLDEN_LEGGINGS(Material.GOLDEN_LEGGINGS, KitCategory.ARMOUR, 2),
    GOLDEN_BOOTS(Material.GOLDEN_BOOTS, KitCategory.ARMOUR, 2),

    CHAINMAIL_HELMET(Material.CHAINMAIL_HELMET, KitCategory.ARMOUR, 3),
    CHAINMAIL_CHESTPLATE(Material.CHAINMAIL_CHESTPLATE, KitCategory.ARMOUR, 3),
    CHAINMAIL_LEGGINGS(Material.CHAINMAIL_LEGGINGS, KitCategory.ARMOUR, 3),
    CHAINMAIL_BOOTS(Material.CHAINMAIL_BOOTS, KitCategory.ARMOUR, 3),

    IRON_HELMET(Material.IRON_HELMET, KitCategory.ARMOUR, 4),
    IRON_CHESTPLATE(Material.IRON_CHESTPLATE, KitCategory.ARMOUR, 4),
    IRON_LEGGINGS(Material.IRON_LEGGINGS, KitCategory.ARMOUR, 4),
    IRON_BOOTS(Material.IRON_BOOTS, KitCategory.ARMOUR, 4),

    DIAMOND_HELMET(Material.DIAMOND_HELMET, KitCategory.ARMOUR, 8),
    DIAMOND_CHESTPLATE(Material.DIAMOND_CHESTPLATE, KitCategory.ARMOUR, 8),
    DIAMOND_LEGGINGS(Material.DIAMOND_LEGGINGS, KitCategory.ARMOUR, 8),
    DIAMOND_BOOTS(Material.DIAMOND_BOOTS, KitCategory.ARMOUR, 8),

    NETHERITE_HELMET(Material.NETHERITE_HELMET, KitCategory.ARMOUR, 25),
    NETHERITE_CHESTPLATE(Material.NETHERITE_CHESTPLATE, KitCategory.ARMOUR, 25),
    NETHERITE_LEGGINGS(Material.NETHERITE_LEGGINGS, KitCategory.ARMOUR, 25),
    NETHERITE_BOOTS(Material.NETHERITE_BOOTS, KitCategory.ARMOUR, 25),

    OBSIDIAN(Material.OBSIDIAN, KitCategory.BLOCK, 5),
    COBBLESTONE(Material.COBBLESTONE, KitCategory.BLOCK, 1),
    COBWEB(Material.COBWEB, KitCategory.BLOCK, 10),
    IRON_DOOR(Material.IRON_DOOR, KitCategory.BLOCK, 1),
    OAK_DOOR(Material.OAK_DOOR, KitCategory.BLOCK, 1),
    OAK_PLANKS(Material.OAK_PLANKS, KitCategory.BLOCK, 1),
    OAK_LOG(Material.OAK_LOG, KitCategory.BLOCK, 1),
    CRAFTING_TABLE(Material.CRAFTING_TABLE, KitCategory.BLOCK, 1),
    WHITE_WOOL(Material.WHITE_WOOL, KitCategory.BLOCK, 1),
    BONE_BLOCK(Material.BONE_BLOCK, KitCategory.BLOCK, 1),
    SANDSTONE(Material.SANDSTONE, KitCategory.BLOCK, 1),
    GRAVEL(Material.GRAVEL, KitCategory.BLOCK, 1),
    ICE(Material.ICE, KitCategory.BLOCK, 1),
    PACKED_ICE(Material.PACKED_ICE, KitCategory.BLOCK, 1),
    BLUE_ICE(Material.BLUE_ICE, KitCategory.BLOCK, 1),
    OAK_TRAPDOOR(Material.OAK_TRAPDOOR, KitCategory.BLOCK, 1),
    HAY_BLOCK(Material.HAY_BLOCK, KitCategory.BLOCK, 1),
    SOUL_SAND(Material.SOUL_SAND, KitCategory.BLOCK, 1),
    SOUL_SOIL(Material.SOUL_SOIL, KitCategory.BLOCK, 1),
    DIRT(Material.DIRT, KitCategory.BLOCK, 1),
    SAND(Material.SAND, KitCategory.BLOCK, 1),
    STONE(Material.STONE, KitCategory.BLOCK, 1),
    TNT(Material.TNT, KitCategory.BLOCK, 10),
    SCAFFOLDING(Material.SCAFFOLDING, KitCategory.BLOCK, 1),
    SLIME_BLOCK(Material.SLIME_BLOCK, KitCategory.BLOCK, 1),
    HONEY_BLOCK(Material.HONEY_BLOCK, KitCategory.BLOCK, 1),
    GLOWSTONE(Material.GLOWSTONE, KitCategory.BLOCK, 1),
    GLASS(Material.GLASS, KitCategory.BLOCK, 2),
    NETHERRACK(Material.NETHERRACK, KitCategory.BLOCK, 1),
    RAIL(Material.RAIL, KitCategory.BLOCK, 1),
    IRON_BLOCK(Material.IRON_BLOCK, KitCategory.BLOCK, 1),
    GOLD_BLOCK(Material.GOLD_BLOCK, KitCategory.BLOCK, 1),
    BEACON(Material.BEACON, KitCategory.BLOCK, 10),
    COBBLED_DEEPSLATE(Material.COBBLED_DEEPSLATE, KitCategory.BLOCK, 1),
    LAPIS_BLOCK(Material.LAPIS_BLOCK, KitCategory.BLOCK, 1),
    REDSTONE_BLOCK(Material.REDSTONE_BLOCK, KitCategory.BLOCK, 1),
    COPPER_BLOCK(Material.COPPER_BLOCK, KitCategory.BLOCK, 1),
    BIRCH_PLANKS(Material.BIRCH_PLANKS, KitCategory.BLOCK, 1),
    DARK_OAK_PLANKS(Material.DARK_OAK_PLANKS, KitCategory.BLOCK, 1),

    WOODEN_SWORD(Material.WOODEN_SWORD, KitCategory.TOOL, 1),
    WOODEN_AXE(Material.WOODEN_AXE, KitCategory.TOOL, 1),
    WOODEN_PICKAXE(Material.WOODEN_PICKAXE, KitCategory.TOOL, 1),
    WOODEN_SHOVEL(Material.WOODEN_SHOVEL, KitCategory.TOOL, 1),
    WOODEN_HOE(Material.WOODEN_HOE, KitCategory.TOOL, 1),

    GOLDEN_SWORD(Material.GOLDEN_SWORD, KitCategory.TOOL, 2),
    GOLDEN_AXE(Material.GOLDEN_AXE, KitCategory.TOOL, 2),
    GOLDEN_PICKAXE(Material.GOLDEN_PICKAXE, KitCategory.TOOL, 2),
    GOLDEN_SHOVEL(Material.GOLDEN_SHOVEL, KitCategory.TOOL, 2),
    GOLDEN_HOE(Material.GOLDEN_HOE, KitCategory.TOOL, 2),

    STONE_SWORD(Material.STONE_SWORD, KitCategory.TOOL, 3),
    STONE_AXE(Material.STONE_AXE, KitCategory.TOOL, 3),
    STONE_PICKAXE(Material.STONE_PICKAXE, KitCategory.TOOL, 3),
    STONE_SHOVEL(Material.STONE_SHOVEL, KitCategory.TOOL, 3),
    STONE_HOE(Material.STONE_HOE, KitCategory.TOOL, 3),

    IRON_SWORD(Material.IRON_SWORD, KitCategory.TOOL, 4),
    IRON_AXE(Material.IRON_AXE, KitCategory.TOOL, 4),
    IRON_PICKAXE(Material.IRON_PICKAXE, KitCategory.TOOL, 4),
    IRON_SHOVEL(Material.IRON_SHOVEL, KitCategory.TOOL, 4),
    IRON_HOE(Material.IRON_HOE, KitCategory.TOOL, 4),

    DIAMOND_SWORD(Material.DIAMOND_SWORD, KitCategory.TOOL, 6),
    DIAMOND_AXE(Material.DIAMOND_AXE, KitCategory.TOOL, 6),
    DIAMOND_PICKAXE(Material.DIAMOND_PICKAXE, KitCategory.TOOL, 6),
    DIAMOND_SHOVEL(Material.DIAMOND_SHOVEL, KitCategory.TOOL, 6),
    DIAMOND_HOE(Material.DIAMOND_HOE, KitCategory.TOOL, 6),

    NETHERITE_SWORD(Material.NETHERITE_SWORD, KitCategory.TOOL, 17),
    NETHERITE_AXE(Material.NETHERITE_AXE, KitCategory.TOOL, 17),
    NETHERITE_PICKAXE(Material.NETHERITE_PICKAXE, KitCategory.TOOL, 17),
    NETHERITE_SHOVEL(Material.NETHERITE_SHOVEL, KitCategory.TOOL, 17),
    NETHERITE_HOE(Material.NETHERITE_HOE, KitCategory.TOOL, 17),

    SHIELD(Material.SHIELD, KitCategory.TOOL, 2),
    TRIDENT(Material.TRIDENT, KitCategory.TOOL, 5),
    FLINT_AND_STEEL(Material.FLINT_AND_STEEL, KitCategory.TOOL, 2),

    SHEARS(Material.SHEARS, KitCategory.TOOL, 1),
    ENDER_PEARL(Material.ENDER_PEARL, KitCategory.TOOL, 2),
    FIREWORK_ROCKET(Material.FIREWORK_ROCKET, KitCategory.TOOL, 1),

    BUCKET(Material.BUCKET, KitCategory.TOOL, 1),
    WATER_BUCKET(Material.WATER_BUCKET, KitCategory.TOOL, 1),
    LAVA_BUCKET(Material.LAVA_BUCKET, KitCategory.TOOL, 1),

    MILK_BUCKET(Material.MILK_BUCKET, KitCategory.TOOL, 1),
    POWDER_SNOW_BUCKET(Material.POWDER_SNOW_BUCKET, KitCategory.TOOL, 1),
    ARROW(Material.ARROW, KitCategory.TOOL, 1),

    BOW(Material.BOW, KitCategory.TOOL, 1),
    CROSSBOW(Material.CROSSBOW, KitCategory.TOOL, 1),
    FISHING_ROD(Material.FISHING_ROD, KitCategory.TOOL, 1),
    OAK_BOAT(Material.OAK_BOAT, KitCategory.TOOL, 1),
    TNT_MINECART(Material.TNT_MINECART, KitCategory.TOOL, 10),

    ELYTRA(Material.ELYTRA, KitCategory.ARMOUR, 100),
    TURTLE_HELMET(Material.TURTLE_HELMET, KitCategory.ARMOUR, 2),

    COOKED_PORKCHOP(Material.COOKED_PORKCHOP, KitCategory.FOOD, 1),
    GOLDEN_APPLE(Material.GOLDEN_APPLE, KitCategory.FOOD, 5),
    BAKED_POTATO(Material.BAKED_POTATO, KitCategory.FOOD, 1),
    COOKED_BEEF(Material.COOKED_BEEF, KitCategory.FOOD, 2),
    GOLDEN_CARROT(Material.GOLDEN_CARROT, KitCategory.FOOD, 3),
    SWEET_BERRIES(Material.SWEET_BERRIES, KitCategory.FOOD, 0),
    PUMPKIN_PIE(Material.PUMPKIN_PIE, KitCategory.FOOD, 1),
    BREAD(Material.BREAD, KitCategory.FOOD, 1),
    COOKED_CHICKEN(Material.COOKED_CHICKEN, KitCategory.FOOD, 1),
    CHORUS_FRUIT(Material.CHORUS_FRUIT, KitCategory.FOOD, 1),
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

    public KitCategory getCategory() {
        return category;
    }

    public int getCost() {
        return cost;
    }

    public static List<KitItem> getItems(KitCategory category) {
        return Collections.unmodifiableList(itemsByCategory.get(category));
    }
}
