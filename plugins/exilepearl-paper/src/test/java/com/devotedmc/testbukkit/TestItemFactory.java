package com.devotedmc.testbukkit;

import static org.mockito.Mockito.mock;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class TestItemFactory implements ItemFactory {

	@Override
	public ItemMeta getItemMeta(Material material) {
		return getItemMetaIntenal(material);
	}

	@Override
	public boolean isApplicable(ItemMeta meta, ItemStack stack) throws IllegalArgumentException {
		return false;
	}

	@Override
	public boolean isApplicable(ItemMeta meta, Material material) throws IllegalArgumentException {
		return false;
	}

	@Override
	public boolean equals(ItemMeta meta1, ItemMeta meta2) throws IllegalArgumentException {
		return false;
	}

	@Override
	public ItemMeta asMetaFor(ItemMeta meta, ItemStack stack) throws IllegalArgumentException {
		return null;
	}

	@Override
	public ItemMeta asMetaFor(ItemMeta meta, Material material) throws IllegalArgumentException {
		return null;
	}

	@Override
	public Color getDefaultLeatherColor() {
		return null;
	}

    private ItemMeta getItemMetaIntenal(Material material) {
        switch (material) {
        case AIR:
            return null;
        case WRITTEN_BOOK:
        case WRITABLE_BOOK:
        	return mock(BookMeta.class);
        case SKELETON_SKULL:
        case CREEPER_HEAD:
        case PLAYER_HEAD:
        case ZOMBIE_HEAD:
        	return mock(SkullMeta.class);
        case LEATHER_HELMET:
        case LEATHER_CHESTPLATE:
        case LEATHER_LEGGINGS:
        case LEATHER_BOOTS:
        	return mock(LeatherArmorMeta.class);
        case POTION:
        case SPLASH_POTION:
        case LINGERING_POTION:
        case TIPPED_ARROW:
        	return mock(PotionMeta.class);
        case MAP:
        	return mock(PotionMeta.class);
        case FIREWORK_ROCKET:
        	return mock(PotionMeta.class);
        case FIREWORK_STAR:
        	return mock(FireworkEffectMeta.class);
        case ENCHANTED_BOOK:
        	return mock(EnchantmentStorageMeta.class);
        case BLACK_BANNER:
        case BLUE_BANNER:
        case BROWN_BANNER:
        case RED_BANNER:
        case YELLOW_BANNER:
        case GREEN_BANNER:
        case LIME_BANNER:
        case LIGHT_BLUE_BANNER:
        case WHITE_BANNER:
        case PURPLE_BANNER:
        case PINK_BANNER:
        case CYAN_BANNER:
        case GRAY_BANNER:
        case LIGHT_GRAY_BANNER:
        case ORANGE_BANNER:
        case MAGENTA_BANNER:
        case BLACK_WALL_BANNER:
        case BLUE_WALL_BANNER:
        case BROWN_WALL_BANNER:
        case RED_WALL_BANNER:
        case YELLOW_WALL_BANNER:
        case GREEN_WALL_BANNER:
        case LIME_WALL_BANNER:
        case LIGHT_BLUE_WALL_BANNER:
        case WHITE_WALL_BANNER:
        case PURPLE_WALL_BANNER:
        case PINK_WALL_BANNER:
        case CYAN_WALL_BANNER:
        case GRAY_WALL_BANNER:
        case LIGHT_GRAY_WALL_BANNER:
        case ORANGE_WALL_BANNER:
        case MAGENTA_WALL_BANNER:
        	return mock(BannerMeta.class);
        case FURNACE:
        case CHEST:
        case TRAPPED_CHEST:
        case JUKEBOX:
        case DISPENSER:
        case DROPPER:
        case ACACIA_SIGN:
        case ACACIA_WALL_SIGN:
        case BIRCH_SIGN:
        case BIRCH_WALL_SIGN:
        case DARK_OAK_SIGN: 
        case DARK_OAK_WALL_SIGN:
        case JUNGLE_SIGN:
        case JUNGLE_WALL_SIGN:
        case OAK_SIGN:
        case OAK_WALL_SIGN:
        case SPRUCE_WALL_SIGN:
        case SPRUCE_SIGN:
        case SPAWNER:
        case NOTE_BLOCK:
        case PISTON:
        case BREWING_STAND:
        case ENCHANTING_TABLE:
        case COMMAND_BLOCK:
        case CHAIN_COMMAND_BLOCK:
        case REPEATING_COMMAND_BLOCK:
        case BEACON:
        case DAYLIGHT_DETECTOR:
        case HOPPER:
        case COMPARATOR:
        case FLOWER_POT:
        case SHIELD:
        case STRUCTURE_BLOCK:
		return mock(BlockStateMeta.class);
        default:
    		return mock(ItemMeta.class);
        }
    }

	@Override
	public Material updateMaterial(ItemMeta meta, Material material) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}
