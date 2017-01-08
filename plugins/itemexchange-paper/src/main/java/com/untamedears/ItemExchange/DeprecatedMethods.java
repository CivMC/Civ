package com.untamedears.ItemExchange;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("deprecation")
public class DeprecatedMethods {
	public static byte getBlockMeta(BlockState blockState) {
		return blockState.getRawData();
	}

	public static byte getBlockMeta(Block block) {
		return block.getData();
	}
	
	public static void setBlockMeta(BlockState blockState, byte meta) {
		blockState.setRawData(meta);
	}
	
	public static int getItemId(ItemStack itemStack) {
		return itemStack.getTypeId();
	}
	
	public static int getMaterialId(Material material) {
		return material.getId();
	}

	public static Material getMaterialById(int id) {
		return Material.getMaterial(id);
	}
	
	public static int getEnchantmentId(Enchantment enchantment) {
		return enchantment.getId();
	}
	
	public static Enchantment getEnchantmentById(int id) {
		return Enchantment.getById(id);
	}
}
