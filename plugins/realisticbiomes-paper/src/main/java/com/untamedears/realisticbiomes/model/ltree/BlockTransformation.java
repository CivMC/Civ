package com.untamedears.realisticbiomes.model.ltree;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.util.Vector;

import vg.civcraft.mc.civmodcore.api.BlockAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

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
	 */
	public boolean applyAt(Location loc) {
		return applyAt(loc.getBlock());
	}
	
	public boolean applyAt(Block block) {
		if (block.getY() > 255) {
			return false;
		}
		if (!MaterialAPI.isAir(block.getType())) {
			return false;
		}
		block.setType(material);
		if (Tag.LEAVES.isTagged(material)) {
			Leaves leaves = (Leaves) block.getBlockData();
			leaves.setPersistent(true);
		}
		for (Entry<String, String> data : blockData.entrySet()) {
			BlockAPI.setBlockProperty(block, data.getKey(), data.getValue());
		}
		return true;
	}

}
