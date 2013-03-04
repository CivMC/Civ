package com.untamedears.realisticbiomes.listener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import com.untamedears.realisticbiomes.GrowthConfig;

public class PlayerListener implements Listener {
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	// map Material that a user uses to hit the ground to a Material, TreeType, or EntityType
	// that is specified. (ie, hit the ground with some wheat seeds and get a message corresponding
	// to the wheat plant's growth rate
	private static Map<Material, Object> materialAliases = new HashMap<Material, Object>();
	
	static {
		materialAliases.put(Material.SEEDS, Material.CROPS);
		materialAliases.put(Material.WHEAT, Material.CROPS);
		materialAliases.put(Material.CARROT_ITEM, Material.CARROT);
		materialAliases.put(Material.POTATO_ITEM, Material.POTATO);
		materialAliases.put(Material.POISONOUS_POTATO, Material.POTATO);
		
		materialAliases.put(Material.MELON_SEEDS, Material.MELON_STEM);
		materialAliases.put(Material.MELON, Material.MELON_BLOCK);
		materialAliases.put(Material.MELON_BLOCK, Material.MELON_BLOCK);
		materialAliases.put(Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM);
		materialAliases.put(Material.PUMPKIN, Material.PUMPKIN);
		
		materialAliases.put(Material.INK_SACK ,Material.COCOA);
		
		materialAliases.put(Material.CACTUS, Material.CACTUS);
		
		materialAliases.put(Material.SUGAR_CANE, Material.SUGAR_CANE_BLOCK);
		
		materialAliases.put(Material.NETHER_STALK, Material.NETHER_WARTS);
		
		// ----------------- //
		
		materialAliases.put(Material.SAPLING, TreeType.TREE);
		
		materialAliases.put(Material.RED_MUSHROOM, TreeType.RED_MUSHROOM);
		materialAliases.put(Material.BROWN_MUSHROOM, TreeType.BROWN_MUSHROOM);
		
		// ----------------- //
		
		materialAliases.put(Material.EGG, Material.EGG);
		materialAliases.put(Material.FISHING_ROD, EntityType.FISHING_HOOK);
	}
	
	private Map<Object, GrowthConfig> growthConfigs;
	
	public PlayerListener(Map<Object, GrowthConfig> growthConfigs) {
		super();
		
		this.growthConfigs = growthConfigs;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {	
		// right click block with the seeds or plant in hand to see what the status is
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			Object material = materialAliases.get(event.getMaterial());
			
			if (material == null)
				return;
			
			GrowthConfig growthConfig = growthConfigs.get(material);
			if (growthConfig == null)
				return;
			
			double growthAmount = growthConfig.getRate(event.getClickedBlock().getRelative(0,1,0));
			
			String amount = new DecimalFormat("#0.00").format(growthAmount);
			event.getPlayer().sendMessage("§7[Realistic Biomes] Growth rate \""+material.toString()+"\" = "+amount);
		}
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
		if (event.getPlayer().getItemInHand().getType() == Material.STICK) {
			Entity entity = event.getRightClicked();
			
			GrowthConfig growthConfig = growthConfigs.get(entity.getType());
			if (growthConfig == null)
				return;
			
			double growthAmount = growthConfig.getRate(entity.getLocation().getBlock());
			
			String amount = new DecimalFormat("#0.00").format(growthAmount);
			event.getPlayer().sendMessage("§7[Realistic Biomes] Spawn rate \""+entity.getType().toString()+"\" = "+amount);
		}
	}
}
