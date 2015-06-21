package com.untamedears.realisticbiomes;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.entity.EntityType;

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
	
	public GrowthConfig put(Material material, GrowthConfig config) {
		return materialMap.put(material, config);
	}
	
	public boolean containsKey(EntityType entity) {
		return entityMap.containsKey(entity);
	}
	
	public GrowthConfig get(EntityType entity) {
		return entityMap.get(entity);
	}

	public GrowthConfig put(EntityType entity, GrowthConfig config) {
		return entityMap.put(entity, config);
	}
	
	public boolean containsKey(TreeType treeType) {
		return treeTypeMap.containsKey(treeType);
	}

	public GrowthConfig get(TreeType treeType) {
		return treeTypeMap.get(treeType);
	}
	
	public GrowthConfig put(TreeType treeType, GrowthConfig config) {
		return treeTypeMap.put(treeType, config);
	}
}
