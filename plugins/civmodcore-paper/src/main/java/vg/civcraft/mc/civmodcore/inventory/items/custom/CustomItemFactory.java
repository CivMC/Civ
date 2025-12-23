package vg.civcraft.mc.civmodcore.inventory.items.custom;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface CustomItemFactory {
	@Contract(value = "-> new", pure = true)
	@NotNull ItemStack createItem();
}
