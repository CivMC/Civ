package com.untamedears.utils;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.GrowthMap;

public class MaterialAliases {
	// map Material that a user uses to hit the ground to a Material, TreeType, or EntityType
	// that is specified. (ie, hit the ground with some wheat seeds and get a message corresponding
	// to the wheat plant's growth rate
	private static Map<Material, Material> materialAliases = new HashMap<Material, Material>();
	
	static {
		materialAliases.put(Material.SEEDS, Material.CROPS);
		materialAliases.put(Material.WHEAT, Material.CROPS);
		materialAliases.put(Material.CARROT_ITEM, Material.CARROT);
		materialAliases.put(Material.POTATO_ITEM, Material.POTATO);
		materialAliases.put(Material.POISONOUS_POTATO, Material.POTATO);
		
		materialAliases.put(Material.MELON_SEEDS, Material.MELON_STEM);
		materialAliases.put(Material.MELON, Material.MELON_BLOCK);
		materialAliases.put(Material.MELON_BLOCK, Material.MELON_BLOCK);
		materialAliases.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
		materialAliases.put(Material.PUMPKIN, Material.PUMPKIN);
		
		materialAliases.put(Material.INK_SACK, Material.COCOA);
		
		materialAliases.put(Material.CACTUS, Material.CACTUS);
		
		materialAliases.put(Material.SUGAR_CANE, Material.SUGAR_CANE_BLOCK);
		
		materialAliases.put(Material.NETHER_STALK, Material.NETHER_WARTS);
		
		materialAliases.put(Material.EGG, Material.EGG);
	}
	
	private static HashMap<TreeSpecies, TreeType> speciesMap = new HashMap<TreeSpecies, TreeType>();
	static {
		speciesMap.put(TreeSpecies.ACACIA, TreeType.ACACIA);
		speciesMap.put(TreeSpecies.BIRCH, TreeType.BIRCH);
		speciesMap.put(TreeSpecies.DARK_OAK, TreeType.DARK_OAK);
		speciesMap.put(TreeSpecies.GENERIC, TreeType.TREE);
		speciesMap.put(TreeSpecies.JUNGLE, TreeType.JUNGLE);
		speciesMap.put(TreeSpecies.REDWOOD, TreeType.REDWOOD);
	}

	public static Material get(ItemStack itemStack) {
		// if the material isn't aliased, just use the material
		return itemStack.getType();
	}

	public static GrowthConfig getConfig(GrowthMap growthConfigs, ItemStack item) {
		Material material = item.getType();
		if (material == Material.SAPLING) {
			MaterialData data = item.getData();
			if (data instanceof Tree) {
				return growthConfigs.get(speciesMap.get(((Tree)data).getSpecies()));
			}
			
		} else if (material == Material.FISHING_ROD) {
			return growthConfigs.get(EntityType.FISHING_HOOK);

		} else {
			if (material == Material.INK_SACK) {
				// special case inksack/dye: ignore all but brown (cocoa seeds)
				MaterialData data = item.getData();
				if ((data instanceof Dye) && ((Dye)data).getColor() != DyeColor.BROWN) {
					return null;
				}
			}
			return growthConfigs.get(materialAliases.get(material));
		}
		return null;
	}
}
