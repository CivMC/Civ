package com.github.civcraft.donum;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.civcraft.donum.database.DonumDAO;
import com.github.civcraft.donum.inventories.DeliveryInventory;
import com.github.civcraft.donum.misc.ItemMapBlobHandling;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DonumManager {

	private DonumDAO database;
	private Map<UUID, DeliveryInventory> deliveryInventories;
	
	public DonumManager() {
		DonumConfiguration config = Donum.getConfiguration();
		this.database = new DonumDAO(config.getHost(), config.getPort(), config.getDatabaseName(), config.getUser(), config.getPassword());
		this.deliveryInventories = new ConcurrentHashMap<UUID, DeliveryInventory>();
	}
	
	public DeliveryInventory getDeliveryInventory(UUID player) {
		return deliveryInventories.get(player);
	}

	public void loadPlayerData(UUID uuid, Inventory i) {
		Donum.getInstance().debug("Loading data for " + uuid.toString());
		ItemMap currentInv = ItemMapBlobHandling.constructItemMapFromInventory(i);
		int hash = currentInv.hashCode();
		new BukkitRunnable() {

			@Override
			public void run() {
				ItemMap oldInv = database.checkForInventoryInconsistency(uuid, hash);
				if (oldInv != null) {
					Donum.getInstance().info("Found inventory inconsistency for " + uuid.toString());
					handleInventoryInconsistency(uuid, oldInv, currentInv);
				}
			}
		}.runTaskAsynchronously(Donum.getInstance());
		
		new BukkitRunnable() {

			@Override
			public void run() {
				System.out.println("haha");
				Donum.getInstance().debug("Attempting to load delivery inventory for " + uuid.toString());
				ItemMap delivery = database.getDeliveryInventory(uuid);
				System.out.println(delivery.toString());
				deliveryInventories.put(uuid, new DeliveryInventory(uuid, delivery));
				Donum.getInstance().debug("Loaded " + delivery.toString() + " for " + uuid.toString());
			}
		}.runTaskAsynchronously(Donum.getInstance());
	}

	public void savePlayerData(UUID uuid, Inventory inventory) {
		Donum.getInstance().debug("Saving data for " + uuid.toString());
		DeliveryInventory del = deliveryInventories.get(uuid);
		if (del == null) {
			Donum.getInstance().debug(
					"Attempted to remove delivery inventory of " + uuid.toString() + ", but it was already gone");
		} else {
			if (del.isDirty()) {
				new BukkitRunnable() {

					@Override
					public void run() {
						database.updateDeliveryInventory(uuid, del.getInventory(), true);
						deliveryInventories.remove(uuid);

					}
				}.runTaskAsynchronously(Donum.getInstance());
			}
		}
		new BukkitRunnable() {

			@Override
			public void run() {
				database.insertLogoutInventory(uuid, ItemMapBlobHandling.constructItemMapFromInventory(inventory));

			}
		}.runTaskAsynchronously(Donum.getInstance());
	}
	
	public void stageDeliveryAddition(UUID uuid, ItemMap items) {
		new BukkitRunnable() {
			
			@Override
			public void run() {
				database.stageDeliveryAddition(uuid, items);
				
			}
		}.runTaskAsynchronously(Donum.getInstance());
	}
	
	public void saveDeathInventory(UUID uuid, ItemMap inventory) {
		Donum.getInstance().debug("Saving death inventory for " + uuid.toString());
		new BukkitRunnable() {
			
			@Override
			public void run() {
				database.insertDeathInventory(uuid, inventory);
			}
		};
	}

	private void handleInventoryInconsistency(UUID player, ItemMap oldInventory, ItemMap newInventory) {
		Donum.getInstance().info("Creating diff of lost items for " + player);
		Donum.getInstance().debug("Old inventory: " + oldInventory.toString());
		Donum.getInstance().debug("New inventory: " + newInventory.toString());
		// TODO
	}
}
