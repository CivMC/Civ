package com.untamedears.realisticbiomes.persist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.util.StructureGrowDelegate;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
import org.bukkit.material.NetherWarts;
import org.bukkit.util.Vector;

import com.untamedears.realisticbiomes.DropGrouper;
import com.untamedears.realisticbiomes.GrowthMap;
import com.untamedears.realisticbiomes.RealisticBiomes;
import com.untamedears.realisticbiomes.events.RealisticBiomesBlockBreakEvent;
import com.untamedears.realisticbiomes.events.RealisticBiomesBlockGrowEvent;
import com.untamedears.realisticbiomes.events.RealisticBiomesStructureGrowEvent;
import com.untamedears.realisticbiomes.utils.Fruits;
import com.untamedears.realisticbiomes.utils.Trees;

// handles force-growing of crop-type blocks based on a fractional growth amount
public class BlockGrower {

	public static Logger LOG = Logger.getLogger("RealisticBiomes");
	private static int COLUMN_PLANT_BLOCK_COUNT = 3;
	
	// store the total growth stages of plants
	public static HashMap<Material, Integer> growthStages = new HashMap<Material, Integer>();
	static {
		growthStages.put(Material.CROPS, 8);
		growthStages.put(Material.CARROT, 8);
		growthStages.put(Material.POTATO, 8);
		
		growthStages.put(Material.MELON_STEM, 8);
		growthStages.put(Material.PUMPKIN_STEM, 8);
		
		growthStages.put(Material.COCOA, 3);
		
		growthStages.put(Material.NETHER_WARTS, 4);
		growthStages.put(Material.BEETROOT_BLOCK, 8);
	}
	
	private PlantManager plantManager;

	private GrowthMap growthMap;
	
	static List<Vector> surroundingBlocks = new ArrayList<Vector>();
	static {
		surroundingBlocks.add(new Vector(-1,0,0));	// west
		surroundingBlocks.add(new Vector(1,0,0));	// east
		surroundingBlocks.add(new Vector(0,0,-1));	// north
		surroundingBlocks.add(new Vector(0,0,1));	// south
	}
	
	public BlockGrower(PlantManager plantManager, GrowthMap growthMap) {
		this.plantManager = plantManager;
		this.growthMap = growthMap;
	}

	/**
	 * grow the crop or stem found at the given block's coordinates with the amount
	 * between 0 and 1, with 1 being totally mature
	 * @param block Block to grow
	 * @param growth Block's growth, between 0 and 1
	 * @param fruitGrowth Fruit growth, -1 if fruitless of 0 - 1 if stem
	 * @return true if growth was prevented (e.g. trees, do not confuse this with simply not advancing growth stages)
	 */
	public boolean growBlock(Block block, float growth, float fruitGrowth) {
		Integer stages = growthStages.get(block.getType());
		if (stages == null) {
			RealisticBiomes.doLog(Level.FINER, "BlockGrower.growBlock(): no stages for " + block.getType());
			return false;
		}
		
		RealisticBiomes.doLog(Level.FINER, "BlockGrower.growBlock(): growing " + block.getType() + " growth: " 
				+ growth + " fruit: " + fruitGrowth);
		
		if (growth > 1.0f) {
			growth = 1.0f;
		}
		
		byte stage = (byte)(((float)(stages-1))*growth);
		BlockState state = block.getState();
		MaterialData data = state.getData();
		if (data instanceof CocoaPlant) {
			// trust that enum order is sanely declared in order SMALL, MEDIUM, LARGE
			CocoaPlantSize cocoaSize = CocoaPlantSize.values()[stage]; 
			((CocoaPlant)data).setSize(cocoaSize);
		} else if (data instanceof Crops) {
			// trust that enum order is sanely declared in order
			CropState cropSize = CropState.values()[stage]; 
			((Crops)data).setState(cropSize);
		} else if (data instanceof NetherWarts) {
			// trust that enum order is sanely declared in order
			NetherWartsState cropSize = NetherWartsState.values()[stage]; 
			((NetherWarts)data).setState(cropSize);
		} else {
			data.setData(stage);
		}
		// Call wrapper event for growth
		state.setData(data);
		RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesBlockGrowEvent(block, state));
		
		state.update(true, false);
		
		if (fruitGrowth != -1.0) {
			this.growFruit(block, fruitGrowth);
		}
		return false;
	}
	
	/**
	 * Generate tree
	 * @param block
	 * @param growth
	 * @param type
	 * @return true if growth was prevented
	 */
	public boolean generateTree(Block block, float growth, TreeType type) {
		RealisticBiomes.doLog(Level.FINER, "BlockGrower.generateTree(): " + type);
		if (growth < 1.0f) {
			return false;
		}
		
		type = Trees.getAlternativeTree(type, block, growthMap);
		
		ArrayList<BlockState> states = new ArrayList<BlockState>();
		states.add(block.getState());
		block.setType(Material.AIR);
		if (type == TreeType.JUNGLE || type == TreeType.MEGA_REDWOOD || type == TreeType.DARK_OAK) {
			// has proven to be northwest block of 2x2 sapling array
			for (Vector vec: Trees.largeTreeBlocks) {
				Block sapling = block.getLocation().add(vec).getBlock();
				states.add(sapling.getState());
				sapling.setType(Material.AIR);
			}
		}
		
		StructureGrowDelegate sgd = new StructureGrowDelegate(((CraftWorld) block.getWorld()).getHandle());
		if (block.getWorld().generateTree(block.getLocation(), type, sgd)) {
			
			// Call wrapper event.
			RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesStructureGrowEvent(
					block.getLocation(), type, false, null, sgd.getBlocks()));
			for (BlockState state : sgd.getBlocks()) {
				// Since we're using the delegate, we need to force the update ourselves.
				state.update(true, false);
			}
			
			// remove affected 2x2 saplings
			for (int i = 1; i < states.size(); i++) {
				plantManager.removePlant(states.get(i).getBlock());
			}
			return false;
		} else {
			RealisticBiomes.doLog(Level.FINER, "generateTree reset data: " + states.size());
			for (BlockState state: states) {
				state.update(true, false);
			}
			return true;
		}
	}

	public boolean growColumn(Block block, float growth, DropGrouper dropGrouper) {
		RealisticBiomes.doLog(Level.FINER, "BlockGrower.growColumn(): " + growth);

		Material type = block.getType();
		
		if (block.getRelative(BlockFace.DOWN).getType() == type) {
			// if this is not the bottom block, just ignore it. it will grow to 1.0 and get removed eventually
			// TODO: do not allow column blocks other than bottom to be added as Plant, to begin with.
			// would need a check in all places where RealisticBiomes.growPlant is called.
			RealisticBiomes.doLog(Level.WARNING, "BlockGrower.growColumn(): is not bottom block");
			return false;
		}
		
		int stage = (int)(((float)(COLUMN_PLANT_BLOCK_COUNT - 1))*growth);
		for (int i = 0; i < stage; i++) {
			block = block.getRelative(BlockFace.UP);
			
			RealisticBiomes.doLog(Level.FINE, "BlockGrower.growColumn(): stage " + i);
			if (block.getType() == type) {
				RealisticBiomes.doLog(Level.FINE, "BlockGrower.growColumn(): block above is already " + type);
				// continue to looke for a free spot above
				continue;
			} else if (block.getType() != Material.AIR) {
				RealisticBiomes.doLog(Level.FINE, "BlockGrower.growColumn(): block above blocking, prevented growth");
				// block was prevented from growth, will reset cycle
				return true;
			} else if (type == Material.CACTUS && instaBreakCactus(block, dropGrouper)) {
				// broken and prevented from growing, keep tracking
				return true;
			} else {
				// grown successfully
				BlockState preChange = block.getState();
				preChange.setType(type);
				RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesBlockGrowEvent(block, preChange));
				block.setType(type);
			}
		}
		return false;
	}

	private boolean instaBreakCactus(Block topBlock, DropGrouper dropGrouper) {
		for (Vector victor: surroundingBlocks) {
			Block candidate = topBlock.getLocation().add(victor).getBlock();
			if (candidate.getType() != Material.AIR) {
				BlockState preChange = topBlock.getState();
				preChange.setType(Material.CACTUS); // stage change for grow event
				RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesBlockGrowEvent(topBlock, preChange));
				
				topBlock.setType(Material.CACTUS); // temporarily change for break event
				RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesBlockBreakEvent(topBlock, null));
				
				if (dropGrouper != null) {
					topBlock.setType(Material.AIR); // grouper doesn't need it to be real
					dropGrouper.add(topBlock.getLocation(), Material.CACTUS);
				} else {
					topBlock.breakNaturally(); // dropper does
				}
				return true;
			}
		}
		return false;
	}

	private void growFruit(Block block, float fruitGrowth) {
		if (Fruits.hasFruit(block)) {
			return;
		}
		
		if (fruitGrowth < 1.0) {
			return;
		}
		
		Block freeBlock = Fruits.getFreeBlock(block, null);
		if (freeBlock != null) {
			Material toBe = Fruits.getFruit(block.getType());
			
			BlockState preChange = freeBlock.getState();
			preChange.setType(toBe); // stage change for grow event
			RealisticBiomes.plugin.getServer().getPluginManager().callEvent(new RealisticBiomesBlockGrowEvent(freeBlock, preChange));
			
			freeBlock.setType(toBe);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static double getGrowthFraction(Block block) {
		if (block.getType() == Material.SAPLING) {
			return 0.0; // sapling blocks actually have a "ready" flag, but it's not provided from bukkit
		} else if (!growthStages.containsKey(block.getType())) {
			return 0.0;
		}
		
		byte stage;
		MaterialData data = block.getState().getData();
		if (data instanceof CocoaPlant) {
			CocoaPlantSize cocoaSize = ((CocoaPlant) data).getSize();
			switch (cocoaSize) {
				case SMALL:
					stage = 0;
					break;
				case MEDIUM:
					stage = 1;
					break;
				case LARGE:
				default:
					stage = 2;
					break;
			}
		} else if (block.getType() == Material.BEETROOT_BLOCK) {
			// Let Bukkit fit the 4 stages into 8 for us.
			stage = ((Crops) data).getState().getData();
		} else {
			stage = data.getData();
		}
		return (double)stage/(double)(growthStages.get(block.getType())-1);
	}

	public static float getFruitGrowthFraction(Block block, Block fruitBlockToIgnore) {
		return Fruits.hasFruit(block, fruitBlockToIgnore) ? 1.0f : 0.0f;
	}
}
