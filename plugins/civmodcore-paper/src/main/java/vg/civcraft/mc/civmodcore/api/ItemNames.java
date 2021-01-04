package vg.civcraft.mc.civmodcore.api;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * @deprecated Use {@link ItemUtils} instead.
 */
@Deprecated
public final class ItemNames {

	/**
	 * @deprecated Use {@link ItemUtils#getItemName(Material)} instead.
	 */
	@Deprecated
	public static String getItemName(Material material) {
		return ItemUtils.getItemName(material);
	}

	/**
	 * @deprecated Use {@link ItemUtils#getItemName(ItemStack)} instead.
	 */
	@Deprecated
	public static String getItemName(ItemStack item) {
		return ItemUtils.getItemName(item);
	}

	/**
	 * @deprecated Use {@link ItemUtils#hasDisplayName(ItemStack)} instead.
	 */
	@Deprecated
	public static boolean hasDisplayName(ItemStack item) {
		return ItemUtils.hasDisplayName(item);
	}

}
