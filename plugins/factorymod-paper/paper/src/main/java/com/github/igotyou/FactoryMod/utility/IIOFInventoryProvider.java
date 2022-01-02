package com.github.igotyou.FactoryMod.utility;

import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

/**
 * Utility interface for classes that provide separate inventories for inputs, outputs, and fuel.
 */
public interface IIOFInventoryProvider {

	Inventory getInputInventory();
	Inventory getOutputInventory();
	@Nullable Inventory getFuelInventory();

	int getInputCount();
	int getOutputCount();
	int getFuelCount();
	default int getTotalIOFCount() {
		return getInputCount() + getOutputCount() + getFuelCount();
	}

}
