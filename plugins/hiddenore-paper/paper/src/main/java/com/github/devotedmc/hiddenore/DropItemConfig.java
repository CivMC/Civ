package com.github.devotedmc.hiddenore;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;

public class DropItemConfig {
	private ItemStack template;
	private boolean canTransform;
	
	public DropItemConfig(ItemStack template) {
		this.template = template;
		this.canTransform = template.getType().isBlock();
	}
	
	public boolean canTransform() {
		return canTransform;
	}
	
	public ItemStack render(double multiplier) {
		ItemStack is = template.clone();
		is.setAmount((int) Math.round((double) is.getAmount() * multiplier));
		if (is.getAmount() > is.getMaxStackSize()) {
			is.setAmount(is.getMaxStackSize());
		}
		return is;
	}
	
	public static List<DropItemConfig> transform(List<ItemStack> items) {
		ArrayList<DropItemConfig> drops = new ArrayList<DropItemConfig>(items.size());
		
		for (ItemStack item : items) {
			if (item != null) {
				drops.add(new DropItemConfig(item));
			}
		}
		
		return drops;
	}
}
