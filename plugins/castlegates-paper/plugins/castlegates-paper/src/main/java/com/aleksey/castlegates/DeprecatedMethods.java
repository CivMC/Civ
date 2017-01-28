/**
 * @author Aleksey Terzi
 *
 */

package com.aleksey.castlegates;

import org.bukkit.Material;
import org.bukkit.block.Block;

@SuppressWarnings("deprecation")
public class DeprecatedMethods {
	public static Material getMaterial(int materialId) {
		return Material.getMaterial(materialId);
	}

	public static int getMaterialId(Material material) {
		return material.getId();
	}

	public static int getTypeId(Block block) {
		return block.getTypeId();
	}

	public static byte getMeta(Block block) {
		return block.getData();
	}

	public static void setTypeIdAndData(Block block, int type, byte meta) {
		block.setTypeIdAndData(type, meta, true);
	}

	public static void setTypeIdAndData(Block block, Material material, byte meta) {
		block.setTypeIdAndData(material.getId(), meta, true);
	}
}
