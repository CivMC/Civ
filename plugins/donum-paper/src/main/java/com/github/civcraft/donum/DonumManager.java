package com.github.civcraft.donum;

import java.util.Map;
import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.civcraft.donum.database.DonumDAO;
import com.github.civcraft.donum.misc.ItemMapBlobHandling;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

public class DonumManager {

	private DonumDAO database;
	private Map<UUID, DeliveryInventory> deliveryInventories;

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
						database.updateDeliveryInventory(uuid, del.getContent());

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

	private void handleInventoryInconsistency(UUID player, ItemMap oldInventory, ItemMap newInventory) {
		Donum.getInstance().info("Creating diff of lost items for " + player);
		Donum.getInstance().debug("Old inventory: " + oldInventory.toString());
		Donum.getInstance().debug("New inventory: " + newInventory.toString());
		// TODO
	}

	private class DeliveryInventory {
		private UUID owner;
		private ItemMap content;
		private boolean isDirty;

		private DeliveryInventory(UUID owner, ItemMap content) {
			this.owner = owner;
			this.content = content;
			this.isDirty = false;
		}

		private void setDirty() {
			isDirty = true;
		}

		private UUID getOwner() {
			return owner;
		}

		private ItemMap getContent() {
			return content;
		}

		private boolean isDirty() {
			return isDirty;
		}
	}

}
