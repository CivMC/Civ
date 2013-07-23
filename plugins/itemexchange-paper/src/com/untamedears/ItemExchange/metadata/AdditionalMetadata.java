package com.untamedears.ItemExchange.metadata;

import org.bukkit.inventory.ItemStack;

public interface AdditionalMetadata {
	public String serialize();
	public boolean matches(ItemStack item);
	public String getDisplayedInfo();
}
