package com.github.civcraft.donum;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DomumAPI {

	/**
	 * Adds the given itemstack to the given players delivery inventory. This
	 * addition will be done async and the earliest point at which it will take
	 * effect is when the player relogs
	 * 
	 * @param player
	 *            UUID of the player to whichs delivery inventory we want to add
	 *            the item
	 * @param item
	 *            Item to add
	 */
	public static void deliverItem(UUID player, ItemStack item) {
		deliverItem(player, new ItemMap(item));
	}

	/**
	 * Adds the given ItemMap to the given players delivery inventory. This
	 * addition will be done async and the earliest point at which it will take
	 * effect is when the player relogs
	 * 
	 * @param player
	 *            UUID of the player to whichs delivery inventory we want to add
	 *            the items
	 * @param items
	 *            Items to add
	 */
	public static void deliverItem(UUID player, ItemMap items) {
		Donum.getManager().stageDeliveryAddition(player, items);
	}

}
