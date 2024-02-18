package com.untamedears.realisticbiomes.model.gauss.drop;

import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.auto.YamlDataObject;

public class BlockDrop extends YamlDataObject<BlockDrop> {

	private List<ItemStack> drops;
	
	public BlockDrop(Location location, boolean isNew, ItemStack drop) {
		this(location, isNew, Arrays.asList(drop));
	}
	
	public BlockDrop(Location location, boolean isNew, List<ItemStack> drops) {
		super(location, isNew);
		this.drops = drops;
	}
	
	public List<ItemStack> getDrops() {
		return drops;
	}

	@Override
	protected void concreteSerialize(YamlConfiguration config) {
		config.set("drops", drops);
	}
	
	public static BlockDrop deserialize(Location location, YamlConfiguration config) {
		@SuppressWarnings("unchecked")
		List<ItemStack> drops = (List<ItemStack>) config.getList("drops");
		return new BlockDrop(location, false, drops);
	}

}
