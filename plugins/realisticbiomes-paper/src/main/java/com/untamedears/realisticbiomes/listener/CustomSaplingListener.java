package com.untamedears.realisticbiomes.listener;

import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.BlockDropManager;
import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.PlantLogicManager;
import com.untamedears.realisticbiomes.PlantManager;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.model.gauss.drop.BlockDrop;

import vg.civcraft.mc.civmodcore.serialization.NBTCompound;

public class CustomSaplingListener {

	private GrowthConfigManager configManager;
	private BlockDropManager dropManager;
	private PlantLogicManager logicManager;
	private PlantManager plantManager;
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void saplingPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		if (item == null || !Tag.SAPLINGS.isTagged(item.getType())) {
			return;
		}
		NBTCompound compound = NBTCompound.fromItem(item);
		if (!compound.getBoolean("rbsap")) {
			// defaults to false if not set
			return;
		}
		dropManager.setDrops(new BlockDrop(e.getBlock().getLocation(), true, item));
		PlantGrowthConfig growthConfig = configManager.getCustomSaplingConfig(item);
		if (growthConfig == null) {
			return;
		}
		Plant plant = new Plant(e.getBlock().getLocation(), growthConfig);
		plantManager.putPlant(plant);
		logicManager.initGrowthTime(plant);
	}

}
