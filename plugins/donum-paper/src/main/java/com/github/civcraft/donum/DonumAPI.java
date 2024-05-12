package com.github.civcraft.donum;

import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


public class DonumAPI {

	/**
	 * Adds the given itemstack to the given players delivery inventory. If the
	 * player is currently online on the server this is run it will be applied
	 * right away, if he isnt it will be applied the next time the players
	 * delivery data is reloaded, which is usually when he relogs
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
	 * Adds the given itemstack to the given players delivery inventory. If the
	 * player is currently online on the server this is run it will be applied
	 * right away, if he isnt it will be applied the next time the players
	 * delivery data is reloaded, which is usually when he relogs
	 * 
	 * @param player
	 *            UUID of the player to whichs delivery inventory we want to add
	 *            the items
	 * @param items
	 *            Items to add
	 */
	public static void deliverItem(UUID player, ItemMap items) {
		Donum.getManager().addToDeliveryInventory(player, items);
	}

}
