package com.github.devotedmc.hiddenore;

import org.bukkit.inventory.ItemStack;

public class DropItemConfig {
	private ItemStack template;
	
	public DropItemConfig(ItemStack template) {
		this.template = template;
	}
	
	public ItemStack render(double multiplier) {
		ItemStack is = template.clone();
		is.setAmount((int) (is.getAmount() * multiplier));
		if (is.getAmount() > is.getMaxStackSize()) {
			is.setAmount(is.getMaxStackSize());
		}
		return is;
	}
}
