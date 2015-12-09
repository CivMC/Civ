package com.github.igotyou.FactoryMod.multiBlockStructures;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public abstract class MultiBlockStructure {
	protected static LinkedList <BlockFace> allBlockSides;
	protected static LinkedList <BlockFace> northEastWestSouthSides;
	
	public static void initiliazeBlockSides() {
		northEastWestSouthSides = new LinkedList<BlockFace>();
		northEastWestSouthSides.add(BlockFace.EAST);
		northEastWestSouthSides.add(BlockFace.NORTH);
		northEastWestSouthSides.add(BlockFace.SOUTH);
		northEastWestSouthSides.add(BlockFace.WEST);
		allBlockSides = new LinkedList<BlockFace>(northEastWestSouthSides);
		allBlockSides.add(BlockFace.DOWN);
		allBlockSides.add(BlockFace.UP);
		
	}
	
	protected static List <Block> searchForBlockOnSides(Block b, Material m) {
		LinkedList <Block>result = new LinkedList<Block>();
		for(BlockFace face:northEastWestSouthSides) {
			Block side = b.getRelative(face);
			if (side.getType() == m) {
				result.add(side);
			}
		}
		return result;
	}
	
	public abstract boolean isComplete();
	
	public abstract void initializeBlocks(Block start); 
	
	public abstract List <Block> getAllBlocks();
		

}
