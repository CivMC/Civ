package com.untamedears.realisticbiomes.listener;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.GrowthMap;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.realisticbiomes.utils.Fruits;
import com.untamedears.realisticbiomes.utils.MaterialAliases;

public class PlayerListener implements Listener {
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	private RealisticBiomes plugin;
	
	
	
	
	private GrowthMap growthConfigs;
	
	public PlayerListener(RealisticBiomes plugin, GrowthMap growthConfigs) {
		this.plugin = plugin;
		this.growthConfigs = growthConfigs;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		// right click block with the seeds or plant in hand to see what the status is
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		if (event.getItem() == null) {
			return;
		}
		
		Plant plant = null;
		
		Block block = event.getClickedBlock();
		
		GrowthConfig growthConfig;
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// hit the ground with a seed, or other farm product: get the adjusted crop growth
			// rate as if that crop was planted on top of the block
			
			if (((List<ItemStack>)block.getDrops()).get(0).getType() == event.getMaterial()) {
				return;
			}
			
			growthConfig = MaterialAliases.getConfig(growthConfigs, event.getItem());
			if (growthConfig == null) {
				RealisticBiomes.doLog(Level.FINER, "No config found for \"" + event.getItem() + "\" : " + growthConfigs.keySet());
				return;
			}
			
			RealisticBiomes.doLog(Level.FINER, "LEFT CLICK: " + event.getItem() + ", " + growthConfig);
			
			if (event.getItem().getType() == Material.INK_SACK) {
				// if dye assume cocoa, otherwise would have exited earlier when growthConfig was null
				block = block.getRelative(event.getBlockFace());
			} else {
				block = block.getRelative(0,1,0);
			}
			
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& (event.getItem().getType() == Material.STICK || event.getItem().getType() == Material.BONE)) {
			
			// right click on a growing crop with a stick: get information about that crop
			growthConfig = MaterialAliases.getConfig(growthConfigs, event.getClickedBlock());
			if (growthConfig == null) {
				return;
			}
			
			if (!Fruits.isFruit(event.getClickedBlock().getType())) {
				if (plugin.persistConfig.enabled && growthConfig != null && growthConfig.isPersistent()) {				
					plant = plugin.growAndPersistBlock(block, false, growthConfig, null, null);
				}
			}
			
		} else {
			// right clicked without stick or bone, do nothing
			return;
		}
		
		// show growth rate information if the item in the player's hand is not a bone
		if (event.getMaterial() == Material.BONE) {
			return;
		}
		
		String materialName = growthConfig.getName();

		if (plugin.persistConfig.enabled && growthConfig.isPersistent()) {
			double rate = growthConfig.getRate(block);
			RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): rate for " + materialName + " at block " + block + " is " + rate);
			rate = (1.0/(rate*(60.0*60.0/*seconds per hour*/)));
			RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): rate adjusted to "  + rate);
			
			if (plant == null) {
				String amount = new DecimalFormat("#0.00").format(rate);
				event.getPlayer().sendMessage("§7[Realistic Biomes] \"" + materialName + "\": "+amount+" hours to maturity");
				
			} else if (plant.getGrowth() == 1.0) {
				if (Fruits.isFruitFul(block.getType())) {
					
					if (!Fruits.hasFruit(block)) {
						Material fruitMaterial = Fruits.getFruit(event.getClickedBlock().getType());
						growthConfig = growthConfigs.get(fruitMaterial);
						if (growthConfig.isPersistent()) {
							block = Fruits.getFreeBlock(event.getClickedBlock(), null);
							if (block != null) {
								double fruitRate = growthConfig.getRate(block);
								RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): fruit rate for block " + block + " is " + fruitRate);
								fruitRate = (1.0/(fruitRate*(60.0*60.0/*seconds per hour*/)));
								RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): fruit rate adjusted to "  + fruitRate);
								
								String amount = new DecimalFormat("#0.00").format(fruitRate);
								String pAmount = new DecimalFormat("#0.00").format(fruitRate*(1.0-plant.getFruitGrowth()));
								event.getPlayer().sendMessage("§7[Realistic Biomes] \""+growthConfig.getName()+"\": "+pAmount+" of "+amount+" hours to maturity");
								return;
							}
						}
					}
					
				}
				
				RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): plant fruit is: " + plant.getFruitGrowth());
				String amount = new DecimalFormat("#0.00").format(rate);
				event.getPlayer().sendMessage("§7[Realistic Biomes] \"" + materialName + "\": "+amount+" hours to maturity");

			} else {
				
				RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): plant growth is: " + plant.getGrowth());
				String amount = new DecimalFormat("#0.00").format(rate);
				String pAmount = new DecimalFormat("#0.00").format(rate*(1.0-plant.getGrowth()));
				event.getPlayer().sendMessage("§7[Realistic Biomes] \"" + materialName + "\": "+pAmount+" of "+amount+" hours to maturity");
			}
			
		} else {
			// Persistence is not enabled
			double growthAmount = growthConfig.getRate(block);
			
			// clamp the growth value between 0 and 1 and put into percent format
			if (growthAmount > 1.0)
				growthAmount = 1.0;
			else if (growthAmount < 0.0)
				growthAmount = 0.0;
			String amount = new DecimalFormat("#0.00").format(growthAmount*100.0)+"%";
			
			String rateType;
			if (growthConfig.getType() == GrowthConfig.Type.ENTITY) {
				rateType = "Spawn rate";
			} else if (growthConfig.getType() == GrowthConfig.Type.FISHING_DROP) {
				if (growthAmount == 0.0) {
					return; // don't spam players clicking things unless it can actually be fished
				}
				rateType = "Fishing rate";
			} else {
				rateType = "Growth rate";
			}
			// send the message out to the user!
			event.getPlayer().sendMessage("§7[Realistic Biomes] " + rateType + " \"" + materialName + "\" = "+amount);
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
			
			// clamp the growth value between 0 and 1 and put into percent format
			if (growthAmount > 1.0)
				growthAmount = 1.0;
			else if (growthAmount < 0.0)
				growthAmount = 0.0;
			String amount = new DecimalFormat("#0.00").format(growthAmount*100.0)+"%";
			// send the message out to the user!
			event.getPlayer().sendMessage("§7[Realistic Biomes] Spawn rate \""+growthConfig.getName()+"\" = "+amount);
		}
	}
}
