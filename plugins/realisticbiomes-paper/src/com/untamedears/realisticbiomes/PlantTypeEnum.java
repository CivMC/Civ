//
// Realistic Biomes
// created march 25 2014
//
package com.untamedears.realisticbiomes;

/**
 * Enum to describe the type of plant that a 'plant' represents
 * @author Mark
 *
 */
public enum PlantTypeEnum {
	
	// the enum definition has to be first
	WHEAT_CROP(0),
	CARROT(1),
	POTATO(2),
	MELON(3),
	PUMPKIN(4),
	SUGAR_CANE(5),
	CACTUS(6),
	COCOA_BEANS(7),
	NETHER_WART(8),
	TREE_OAK(9),
	TREE_SPRUCE(10),
	TREE_BIRCH(11),
	TREE_JUNGLE(12),
	TREE_ACACIA(13),
	TREE_DARKOAK(14);
	
	private final int num;
	
	
	/**
	 * constructor
	 * @param numVal - the 'enum value' that we are manually specifying
	 */
	private PlantTypeEnum(int numVal) {
		
		this.num = numVal;
		
	}


	/**
	 * returns the enum's value
	 * @return the numeric value of this enum
	 */
	public int getValue() {
		return num;
	}
	
	/**
	 * returns whether or not the specified enum is one of the tree types
	 * @param enumVal - the enum value to compare
	 * @return boolean whether or not the specified enum is a tree 
	 */
	public boolean isTree(PlantTypeEnum enumVal) {
		
		
		return enumVal == TREE_OAK ||
			enumVal == TREE_SPRUCE ||
			enumVal == TREE_BIRCH ||
			enumVal == TREE_JUNGLE ||
			enumVal == TREE_ACACIA ||
			enumVal == TREE_DARKOAK;
		
	}
		
	

}
