package com.untamedears.JukeAlert;

import org.bukkit.Material;

@SuppressWarnings("deprecation")
public class DeprecatedMethods {
	public static int getMaterialId(Material material) {
		return material.getId();
	}

	public static Material getMaterialById(int id) {
		return Material.getMaterial(id);
	}
}