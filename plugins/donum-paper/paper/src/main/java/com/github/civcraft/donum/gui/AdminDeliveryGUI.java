package com.github.civcraft.donum.gui;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import vg.civcraft.mc.namelayer.NameAPI;

public class AdminDeliveryGUI {
	
	//admin uuid is key, guy to deliver to is value
	private static HashMap<UUID, UUID> deliveryInventoriesEdited = new HashMap<UUID, UUID>();;
	
	public static void showInventory(Player admin, UUID target) {
		deliveryInventoriesEdited.put(admin.getUniqueId(), target);
		Inventory inventory = Bukkit.createInventory(null, 54, NameAPI.getCurrentName(target));
		admin.openInventory(inventory);
	}
	
	public static UUID getPlayerBeingViewed(UUID adminUUID) {
		return deliveryInventoriesEdited.get(adminUUID);
	}
	
	public static boolean isEditingPlayerDeliveryInventory(UUID adminUUID) {
		return deliveryInventoriesEdited.get(adminUUID) != null;
	}
	
	public static void closedInventory(UUID admin) {
		deliveryInventoriesEdited.remove(admin);
	}
}
