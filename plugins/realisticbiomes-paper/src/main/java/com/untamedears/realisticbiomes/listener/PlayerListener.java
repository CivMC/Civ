package com.untamedears.realisticbiomes.listener;

import java.text.DecimalFormat;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.AnimalConfigManager;
import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.AnimalMateConfig;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.RBUtils;

public class PlayerListener implements Listener {

	private GrowthConfigManager growthConfigs;
	private AnimalConfigManager animalManager;
	private DecimalFormat decimalFormat = new DecimalFormat("0.####");

	public PlayerListener(GrowthConfigManager growthConfigs, AnimalConfigManager animalManager) {
		this.growthConfigs = growthConfigs;
		this.animalManager = animalManager;
	}

	// show plant progress when right clicking it with stick
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractCrop(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (event.getItem() == null) {
			return;
		}
		if (event.getItem().getType() != Material.STICK) {
			return;
		}
		Block block = RBUtils.getRealPlantBlock(event.getClickedBlock());
		if (RBUtils.isFruit(block.getType())) {
			return;
		}
		PlantGrowthConfig plantConfig = growthConfigs.getPlantGrowthConfig(block);
		if (plantConfig == null) {
			return;
		}
		Plant plant = null;
		if (RealisticBiomes.getInstance().getPlantManager() != null) {
			plant = RealisticBiomes.getInstance().getPlantManager().getPlant(block);
		}
		event.getPlayer().sendMessage(plantConfig.getPlantInfoString(block, plant));
	}

	// show animal rates when right clicking them with stick
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item.getType() != Material.STICK) {
			return;
		}
		Entity entity = event.getRightClicked();
		if (!(entity instanceof Animals)) {
			return;
		}
		AnimalMateConfig animalConfig = animalManager.getAnimalMateConfig(entity.getType());
		double rate;
		if (animalConfig == null) {
			rate = 1.0;
		} else {
			rate = animalConfig.getRate(entity.getLocation().getBlock().getBiome());
		}
		event.getPlayer().sendMessage(ChatColor.GOLD + decimalFormat.format(rate * 100) + "% spawn chance for "
				+ entity.getType().toString());
	}

	// show growth rates when hitting floor with crop
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
			return;
		}
		if (event.getItem() == null) {
			return;
		}
		Material material = RBUtils.getRemappedMaterial(event.getItem().getType());
		if (material == null) {
			return;
		}
		PlantGrowthConfig plantConfig = growthConfigs.getGrowthConfigStraight(material);
		if (plantConfig == null) {
			event.getPlayer().sendMessage(
					ChatColor.GOLD + "Growth behavior for " + material.toString() + " is entirely vanilla");
			return;
		}
		event.getPlayer()
				.sendMessage(plantConfig.getInfoString(event.getClickedBlock().getRelative(event.getBlockFace())));
	}
}
