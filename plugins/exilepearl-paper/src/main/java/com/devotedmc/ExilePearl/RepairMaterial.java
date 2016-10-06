package com.devotedmc.ExilePearl;

import org.bukkit.inventory.ItemStack;

import com.devotedmc.ExilePearl.util.Guard;

public class RepairMaterial {

	final ItemStack stack;
	final int repairAmount;
	
	public RepairMaterial(final ItemStack stack, final int repairAmount) {
		Guard.ArgumentNotNull(stack, "stack");
		
		this.stack = stack;
		this.repairAmount = repairAmount;
	}
	
	public ItemStack getStack() {
		return stack;
	}
	
	public int getRepairAmount() {
		return repairAmount;
	}
	
}
