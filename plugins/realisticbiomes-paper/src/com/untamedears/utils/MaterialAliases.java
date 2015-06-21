package com.untamedears.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;
import org.bukkit.material.Tree;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.GrowthMap;
import com.untamedears.realisticbiomes.RealisticBiomes;

public class MaterialAliases {
	// map Material that a user uses to hit the ground to a Material, TreeType,
	// or EntityType
	// that is specified. (ie, hit the ground with some wheat seeds and get a
	// message corresponding
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

		materialAliases.put(Material.FISHING_ROD, Material.RAW_FISH);
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
	
//	private static HashMap<EntityMaterialAlias, EntityType> entityMap = new HashMap<EntityMaterialAlias, EntityType>();
//	static {
//		entityMap.put(new EntityMaterialAlias(Material.EGG, null), EntityType.CHICKEN);
//		entityMap.put(new EntityMaterialAlias(Material.MONSTER_EGG, new SpawnEgg(EntityType.WOLF)), EntityType.CHICKEN);
//	}

	public static Material get(ItemStack itemStack) {
		// if the material isn't aliased, just use the material
		return itemStack.getType();
	}

	public static GrowthConfig getConfig(GrowthMap growthConfigs,
			ItemStack item) {
		Material material = item.getType();
		if (material == Material.SAPLING) {
			MaterialData data = item.getData();
			RealisticBiomes.doLog(Level.FINER, "Special case sapling: " + data);
			if (data instanceof Tree) {
				if (!growthConfigs.containsKey(speciesMap.get(((Tree) data).getSpecies()))) {
					RealisticBiomes.doLog(Level.FINER, "No such tree: " + speciesMap.get(((Tree) data).getSpecies()));
				}
				return growthConfigs.get(speciesMap.get(((Tree) data)
						.getSpecies()));
			}
		
		} else if (material == Material.EGG) {
			// special case egg maps to chicken (not a spawn egg)
			return growthConfigs.get(EntityType.CHICKEN);
			
		} else if (material == Material.MONSTER_EGG) {
			MaterialData data = item.getData();
			if (data instanceof SpawnEgg) {
				return growthConfigs.get(((SpawnEgg)data).getSpawnedType());
			}

		} else {
			if (!isCocoa(material, item.getData())) {
				return null;
			}
			
			if (materialAliases.containsKey(material)) {
				return growthConfigs.get(materialAliases.get(material));
			}
		}
		// if not aliased try to get config directly from itemStack
		return growthConfigs.get(material);
	}

	public static boolean isCocoa(Material material, MaterialData data) {
		return material == Material.INK_SACK && data instanceof Dye && ((Dye)data).getColor() == DyeColor.BROWN;
	}
}
