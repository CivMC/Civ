package com.untamedears.realisticbiomes.model.ltree;

import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import vg.civcraft.mc.civmodcore.inventory.items.MaterialUtils;
import vg.civcraft.mc.civmodcore.world.BlockProperties;

public class BlockTransformation {
	private Material material;
	private Map<String, String> blockData;

	public BlockTransformation(Material material, Map<String, String> blockData) {
		this.material = material;
		this.blockData = blockData;
	}

	/**
	 * Applies this transformation at the given location
	 * 
	 * 
	 * @param loc       Location the current LStep is at
	 * @return Whether the transformation could be applied
	 */
	public boolean applyAt(Location loc) {
		return applyAt(loc.getBlock());
	}
	
	public boolean applyAt(Block block) {
		if (block.getY() > 255) {
			return false;
		}
		if (!MaterialUtils.isAir(block.getType())) {
			return false;
		}
		block.setType(material);
		if (Tag.LEAVES.isTagged(material)) {
			Leaves leaves = (Leaves) block.getBlockData();
			leaves.setPersistent(true);
		}
		for (Entry<String, String> data : blockData.entrySet()) {
			BlockProperties.setBlockProperty(block, data.getKey(), data.getValue());
		}
		return true;
	}

}
