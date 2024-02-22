package com.github.maxopoly.finale.misc;

import org.bukkit.inventory.ItemStack;

public enum Slot {

	MAIN_HAND("mainhand", 894654, 2872),
	OFF_HAND("offhand", 894654, 2872),
	HELMET("head", 1, 1),
	CHESTPLATE("chest", 1, 2),
	LEGGINGS("legs", 2, 1),
	BOOTS("feet", 2, 2);
	
	private String name;
	private int uuidLeast;
	private int uuidMost;
	
	private Slot(String name, int uuidLeast, int uuidMost) {
		this.name = name;
		this.uuidLeast = uuidLeast;
		this.uuidMost = uuidMost;
	}
	
	public String getName() {
		return name;
	}
	
	public int getUuidLeast() {
		return uuidLeast;
	}
	
	public int getUuidMost() {
		return uuidMost;
	}
	
	public static Slot getArmourSlot(ItemStack is) {
		if (ItemUtil.isHelmet(is)) {
			return HELMET;
		} else if (ItemUtil.isChestplate(is)) {
			return CHESTPLATE;
		} else if (ItemUtil.isLeggings(is)) {
			return LEGGINGS;
		} else if (ItemUtil.isBoots(is)) {
			return BOOTS;
		} else {
			throw new IllegalArgumentException("Unable to determine the armour slot of given item stack."); 
		}
	}
	
}
