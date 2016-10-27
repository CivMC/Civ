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
        case BOOK_AND_QUILL:
        	return mock(BookMeta.class);
        case SKULL_ITEM:
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
        case FIREWORK:
        	return mock(PotionMeta.class);
        case FIREWORK_CHARGE:
        	return mock(FireworkEffectMeta.class);
        case ENCHANTED_BOOK:
        	return mock(EnchantmentStorageMeta.class);
        case BANNER:
        	return mock(BannerMeta.class);
        case FURNACE:
        case CHEST:
        case TRAPPED_CHEST:
        case JUKEBOX:
        case DISPENSER:
        case DROPPER:
        case SIGN:
        case MOB_SPAWNER:
        case NOTE_BLOCK:
        case PISTON_BASE:
        case BREWING_STAND_ITEM:
        case ENCHANTMENT_TABLE:
        case COMMAND:
        case COMMAND_REPEATING:
        case COMMAND_CHAIN:
        case BEACON:
        case DAYLIGHT_DETECTOR:
        case DAYLIGHT_DETECTOR_INVERTED:
        case HOPPER:
        case REDSTONE_COMPARATOR:
        case FLOWER_POT_ITEM:
        case SHIELD:
        case STRUCTURE_BLOCK:
		return mock(BlockStateMeta.class);
        default:
    		return mock(ItemMeta.class);
        }
    }

}
