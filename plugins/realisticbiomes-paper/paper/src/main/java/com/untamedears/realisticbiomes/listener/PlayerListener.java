package com.untamedears.realisticbiomes.listener;

import com.untamedears.realisticbiomes.AnimalConfigManager;
import com.untamedears.realisticbiomes.GrowthConfigManager;
import com.untamedears.realisticbiomes.PlantManager;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.growthconfig.AnimalMateConfig;
import com.untamedears.realisticbiomes.growthconfig.PlantGrowthConfig;
import com.untamedears.realisticbiomes.model.Plant;
import com.untamedears.realisticbiomes.utils.Constants;
import com.untamedears.realisticbiomes.utils.RBUtils;
import java.text.DecimalFormat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

	private GrowthConfigManager growthConfigs;
	private AnimalConfigManager animalManager;
	private PlantManager plantManager;
	private DecimalFormat decimalFormat = new DecimalFormat("0.####");

	public PlayerListener(GrowthConfigManager growthConfigs, AnimalConfigManager animalManager,
			PlantManager plantManager) {
		this.growthConfigs = growthConfigs;
		this.animalManager = animalManager;
		this.plantManager = plantManager;
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
		if (event.getItem().getType() != Constants.Stick) {
			return;
		}
		Block block = RBUtils.getRealPlantBlock(event.getClickedBlock());
		if (RBUtils.isFruit(block.getType())) {
			return;
		}
		Plant plant = plantManager.getPlant(block);
		if (plant == null) {
			PlantGrowthConfig growthConfig = growthConfigs.getGrowthConfigFallback(block.getType());
			if (growthConfig == null) {
				return;
			}
			if (RBUtils.isFruit(block.getType())) {
				return;
			}
			if (growthConfig.isPersistent()) {
				// a plant should be here, but isn't
				plant = new Plant(block.getLocation(), growthConfig);
				plantManager.putPlant(plant);
			} else {
				return;
			}
		}
		PlantGrowthConfig plantConfig = plant.getGrowthConfig();
		if (plantConfig == null) {
			plantConfig = growthConfigs.getPlantGrowthConfigFallback(plant);
			if (plantConfig == null) {
				return;
			}
			if (plantConfig.isPersistent()) {
				plant.setGrowthConfig(plantConfig);
			} else {
				// a plant with no growth config and no fallback for it could be determined, so
				// deleted it
				plantManager.deletePlant(plant);
				return;
			}
		}
		RealisticBiomes.getInstance().getPlantLogicManager().updateGrowthTime(plant, block);
		event.getPlayer().sendMessage(plantConfig.getPlantInfoString(block, plant));
		if (event.getPlayer().hasPermission("rb.op")) {
			event.getPlayer().sendMessage(plant.toString());
		}
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
		ItemStack item = event.getItem();
		PlantGrowthConfig plantConfig = growthConfigs.getGrowthConfigByItem(item);
		if (plantConfig == null) {
			return;
		}
		event.getPlayer()
				.sendMessage(plantConfig.getInfoString(event.getClickedBlock().getRelative(event.getBlockFace())));
	}
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPlayerHarvest(PlayerHarvestBlockEvent event) {
		if (event.getHarvestedBlock().getType() != Material.SWEET_BERRY_BUSH) {
			return;
		}
		Block block = event.getHarvestedBlock();
		Plant plant = RealisticBiomes.getInstance().getPlantManager().getPlant(block);
		if (plant == null) {
			return;
		}
		PlantGrowthConfig config = plant.getGrowthConfig();
		if (config == null) {
			return;
		}
		config.getGrower().setStage(plant, 0);

		plant.setCreationTime(System.currentTimeMillis());
		Bukkit.getScheduler().runTask(RealisticBiomes.getInstance(), () -> {
			RealisticBiomes.getInstance().getPlantLogicManager().updateGrowthTime(plant, block);
		});
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onFish(PlayerFishEvent event) {
		//disable prot 4 fishing
		if (event.getCaught() == null) {
			return;
		}
		if (event.getCaught() instanceof Item item) {
			Material mat = item.getItemStack().getType();
			if (mat == Material.BOW ||
				mat == Material.ENCHANTED_BOOK ||
				mat == Material.FISHING_ROD) {

				event.setCancelled(true);
			}
		}
	}
}
