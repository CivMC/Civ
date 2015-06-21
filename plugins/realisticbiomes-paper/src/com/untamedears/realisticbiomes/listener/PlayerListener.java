package com.untamedears.realisticbiomes.listener;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Dye;
import org.bukkit.material.MaterialData;

import com.untamedears.realisticbiomes.GrowthConfig;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.persist.Plant;
import com.untamedears.utils.Fruits;

public class PlayerListener implements Listener {
	
	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	
	private RealisticBiomes plugin;
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
	
	private static HashMap<Integer, TreeType> saplingIndexMap;
	
	static {
		saplingIndexMap = new HashMap<Integer, TreeType>();
		
		saplingIndexMap.put(new Integer(0), TreeType.TREE);
		saplingIndexMap.put(new Integer(1), TreeType.REDWOOD);
		saplingIndexMap.put(new Integer(2), TreeType.BIRCH);
		saplingIndexMap.put(new Integer(3), TreeType.JUNGLE);
		saplingIndexMap.put(new Integer(4), TreeType.ACACIA);
		saplingIndexMap.put(new Integer(5), TreeType.DARK_OAK);
	}
	
	private Map<Object, GrowthConfig> growthConfigs;
	
	public PlayerListener(RealisticBiomes plugin, Map<Object, GrowthConfig> growthConfigs) {
		this.plugin = plugin;
		this.growthConfigs = growthConfigs;
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteractEvent(PlayerInteractEvent event) {	
		Plant plant = null;
		
		// right click block with the seeds or plant in hand to see what the status is
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
			
		Object material = event.getMaterial()/*in hand*/;
		Block block = event.getClickedBlock();
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// hit the ground with a seed, or other farm product: get the adjusted crop growth
			// rate as if that crop was planted on top of the block
			material = materialAliases.get(material);
			// if the material isn't aliased, just use the material
			if (material == null)
				material = event.getMaterial();
			
			// handle saplings as their tree types
			if (event.getItem() != null && event.getItem().getTypeId() == Material.SAPLING.getId()) {
				int data = event.getItem().getData().getData();
				if (saplingIndexMap.containsKey(data)) {
					material = saplingIndexMap.get(data);
				}
			}
			
//			RealisticBiomes.doLog(Level.FINER, "CLICKY: " + event.getItem().getData());
			
			// don't do anything if the material is a dye, but not cocoa brown
			if (event.getMaterial() == Material.INK_SACK) {
				MaterialData data = event.getItem().getData();
				if ((data instanceof Dye) && ((Dye)data).getColor() != DyeColor.BROWN) {
					return;
				}
			}
			
		} else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && (material == Material.STICK || material == Material.BONE)) {
			// right click on a growing crop with a stick: get information about that crop
			material = event.getClickedBlock().getType();
			
			// handle saplings as their tree types
			int index = block.getData();
			if (material == Material.SAPLING && saplingIndexMap.containsKey(index)) {
				
				material = saplingIndexMap.get(index);
			}
			
			if (!Fruits.isFruit((Material) material)) {
				GrowthConfig growthConfig = growthConfigs.get(material);
				if (plugin.persistConfig.enabled && growthConfig != null && growthConfig.isPersistent()) {
					
					plant = plugin.growAndPersistBlock(block, false, growthConfig, null);
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
		
		if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
			// from hitting something with material in hand
			if (material == Material.COCOA) {
				block = block.getRelative(event.getBlockFace());
			} else {
				block = block.getRelative(0,1,0);
			}
		}
		
		GrowthConfig growthConfig = growthConfigs.get(material);
		if (growthConfig == null) {
			return;
		}

		if (plugin.persistConfig.enabled && growthConfig.isPersistent()) {
			double rate = growthConfig.getRate(block);
			RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): rate for " + material + " at block " + block + " is " + rate);
			rate = (1.0/(rate*(60.0*60.0/*seconds per hour*/)));
			RealisticBiomes.doLog(Level.FINER, "PlayerListener.onPlayerInteractEvent(): rate adjusted to "  + rate);
			
			if (plant == null) {
				String amount = new DecimalFormat("#0.00").format(rate);
				event.getPlayer().sendMessage("§7[Realistic Biomes] \""+material.toString()+"\": "+amount+" hours to maturity");
				
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
								event.getPlayer().sendMessage("§7[Realistic Biomes] \""+fruitMaterial.toString()+"\": "+pAmount+" of "+amount+" hours to maturity");
								return;
							}
						}
					}
					
				}
				
				
				String amount = new DecimalFormat("#0.00").format(rate);
				event.getPlayer().sendMessage("§7[Realistic Biomes] \""+material.toString()+"\": "+amount+" hours to maturity");

			} else {
				String amount = new DecimalFormat("#0.00").format(rate);
				String pAmount = new DecimalFormat("#0.00").format(rate*(1.0-plant.getGrowth()));
				event.getPlayer().sendMessage("§7[Realistic Biomes] \""+material.toString()+"\": "+pAmount+" of "+amount+" hours to maturity");
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
			// send the message out to the user!
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
			
			// clamp the growth value between 0 and 1 and put into percent format
			if (growthAmount > 1.0)
				growthAmount = 1.0;
			else if (growthAmount < 0.0)
				growthAmount = 0.0;
			String amount = new DecimalFormat("#0.00").format(growthAmount*100.0)+"%";
			// send the message out to the user!
			event.getPlayer().sendMessage("§7[Realistic Biomes] Spawn rate \""+entity.getType().toString()+"\" = "+amount);
		}
	}
}
