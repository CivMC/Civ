package com.untamedears.realisticbiomes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import net.minecraft.server.v1_9_R1.EnchantmentManager;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.GrowthConfig.Type;
import com.untamedears.realisticbiomes.utils.MaterialAliases;

/**
 * Would probably be best to have box type as key, with material + data or entity
 */
public class GrowthMap {
	
	private HashMap<Material, GrowthConfig> materialMap = new HashMap<Material, GrowthConfig>();
	private HashMap<EntityType, GrowthConfig> entityMap = new HashMap<EntityType, GrowthConfig>();
	private HashMap<TreeType, GrowthConfig> treeTypeMap = new HashMap<TreeType, GrowthConfig>();
	
	public boolean containsKey(Material material) {
		return materialMap.containsKey(material);
	}
	
	public GrowthConfig get(Material material) {
		return materialMap.get(material);
	}
	
	public GrowthConfig put(Material material, GrowthConfig config, GrowthConfig.Type type) {
		if (type == null) {
			if (MaterialAliases.isColumnBlock(material)) {
				type = GrowthConfig.Type.COLUMN;
			} else {
				type = GrowthConfig.Type.PLANT;
			}
		}
		return materialMap.put(material, config.setType(type));
	}
	
	/**
	 * Picks from materials only.
	 * @param block holding biome to adjust drop rate by
	 * @param random a random number between 0 and 1
	 * @return a ItemStack based on the Material pulled from the materialMaps.
	 */
	public ItemStack pickOne(Block block, double random) {
		TreeMap<Double, Material> remap = new TreeMap<Double, Material>();
		double cumrate = 0.0d;
		for (Entry<Material, GrowthConfig> e : materialMap.entrySet()) {
			double newrate = e.getValue().getRate(block);
			if (newrate > 0.0) {
				cumrate += newrate;
				remap.put(cumrate, e.getKey());
			}
		}
		if (cumrate > 1.0) {
			random = random * cumrate; // "inflate"
		}
		if (random > cumrate) {
			return null;
		}
		// Find the element that caps this chance, if any.
		Double newkey = remap.ceilingKey(random);
		if (newkey == null) {
			return null;
		}
		Material ret = remap.get(newkey);
		if (ret != null) {
			GrowthConfig gc = materialMap.get(ret);
			ItemStack is = new ItemStack(ret, 1);
			if (gc.getApplyRandomEnchantment()) {
				net.minecraft.server.v1_9_R1.ItemStack nmsis = CraftItemStack.asNMSCopy(is);
				net.minecraft.server.v1_9_R1.ItemStack nmsis2 = EnchantmentManager.a(
						new Random(), nmsis, 30, gc.getAllowTreasureEnchantments());
				is = CraftItemStack.asBukkitCopy(nmsis2);
			}
			
			return is;
		}
		return null;
	}
	
	public boolean containsKey(EntityType entity) {
		return entityMap.containsKey(entity);
	}
	
	public GrowthConfig get(EntityType entity) {
		return entityMap.get(entity);
	}

	public GrowthConfig put(EntityType entity, GrowthConfig config) {
		return entityMap.put(entity, config.setType(GrowthConfig.Type.ENTITY));
	}
	
	public boolean containsKey(TreeType treeType) {
		return treeTypeMap.containsKey(treeType);
	}

	public GrowthConfig get(TreeType treeType) {
		return treeTypeMap.get(treeType);
	}
	
	public GrowthConfig put(TreeType treeType, GrowthConfig config) {
		config.setType(Type.TREE);
		config.setTreeType(treeType);
		return treeTypeMap.put(treeType, config);
	}

	/**
	 * For debugging purposes only
	 */
	public Set<Object> keySet() {
		Set<Object> sets = new HashSet<Object>();
		sets.addAll(materialMap.keySet());
		sets.addAll(entityMap.keySet());
		sets.addAll(treeTypeMap.keySet());
		return sets;
	}

	public void putAll(GrowthMap other) {
		materialMap.putAll(other.materialMap);
		entityMap.putAll(other.entityMap);
		treeTypeMap.putAll(other.treeTypeMap);
	}
}
