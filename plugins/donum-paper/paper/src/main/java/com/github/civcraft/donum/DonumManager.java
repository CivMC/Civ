package com.github.civcraft.donum;

import com.github.civcraft.donum.database.DonumDAO;
import com.github.civcraft.donum.inventories.DeathInventory;
import com.github.civcraft.donum.inventories.DeliveryInventory;
import com.github.civcraft.donum.misc.ItemMapBlobHandling;
import com.github.civcraft.donum.storage.DatabaseStorage;
import com.github.civcraft.donum.storage.IDeliveryStorage;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


public class DonumManager {

	private DonumDAO database;
	private IDeliveryStorage deliveryStorage;
	private Map<UUID, DeliveryInventory> deliveryInventories;

	public DonumManager() {
		DonumConfiguration config = Donum.getConfiguration();
		this.database = new DonumDAO(config.getHost(), config.getPort(), config.getDatabaseName(), config.getUser(),
				config.getPassword());
		deliveryStorage = new DatabaseStorage();
		this.deliveryInventories = new ConcurrentHashMap<UUID, DeliveryInventory>();
	}

	public DeliveryInventory getDeliveryInventory(UUID player) {
		return deliveryInventories.get(player);
	}

	/**
	 * Spawns an async task to load the given players delivery inventory and
	 * check for possible inconsistencies since his last logout. This method
	 * should never be used by anything other than the login listener in this
	 * plugin
	 * 
	 * @param uuid UUID of the player to load
	 * @param i Player inventory on login to compare with the saved logout inventory
	 */
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

		deliveryStorage.loadDeliveryInventory(uuid);
	}

	public void savePlayerData(UUID uuid, Inventory inventory, boolean async) {
		Donum.getInstance().debug("Saving data for " + uuid.toString());
		DeliveryInventory del = deliveryInventories.get(uuid);
		if (del == null) {
			Donum.getInstance().debug(
					"Attempted to remove delivery inventory of " + uuid.toString() + ", but it was already gone");
			return;
		} else {
			deliveryInventories.remove(uuid);
			if (del.isDirty()) {
				deliveryStorage.updateDeliveryInventory(uuid, del.getInventory(), async);
			}
		}
		if (async) {
		new BukkitRunnable() {

			@Override
			public void run() {
				database.insertLogoutInventory(uuid, ItemMapBlobHandling.constructItemMapFromInventory(inventory));

			}
		}.runTaskAsynchronously(Donum.getInstance());
		}
		else {
			database.insertLogoutInventory(uuid, ItemMapBlobHandling.constructItemMapFromInventory(inventory));
		}
	}

	public void addToDeliveryInventory(UUID uuid, ItemMap items) {
		DeliveryInventory inventory = deliveryInventories.get(uuid);
		if (inventory != null) {
			inventory.getInventory().addAll(items.getItemStackRepresentation());
			inventory.setDirty(true);
		} else {
			stageDeliveryAddition(uuid, items);
		}
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
		}.runTaskAsynchronously(Donum.getInstance());
	}
	
	public void setDeliveryInventory(UUID player,ItemMap im) {
		deliveryInventories.put(player, new DeliveryInventory(player, im));
	}

	private void handleInventoryInconsistency(UUID player, ItemMap oldInventory, ItemMap newInventory) {
		Donum.getInstance().info("Creating diff of lost items for " + player);
		Donum.getInstance().debug("Old inventory: " + oldInventory.toString());
		Donum.getInstance().debug("New inventory: " + newInventory.toString());
		ItemMap diff = new ItemMap();
		for (Entry<ItemStack, Integer> entry : oldInventory.getEntrySet()) {
			ItemStack is = entry.getKey();
			int oldAmount = entry.getValue();
			int newAmount = newInventory.getAmount(is);
			if (newAmount > oldAmount) {
				Donum.getInstance().warning(
						"[DUPEALERT]" + player + " had " + oldAmount + " of " + is.toString()
								+ " when logging off and now has " + newAmount);
				continue;
			}
			if (newAmount < oldAmount) {
				diff.addItemAmount(is, oldAmount - newAmount);
			}
		}
		database.insertInconsistency(player, diff);
	}
	
	public void returnDeathInventory(DeathInventory inv) {
		inv.setReturned(true);
		DonumAPI.deliverItem(inv.getOwner(), inv.getInventory());
		new BukkitRunnable() {

			@Override
			public void run() {
				database.updateDeathInventoryReturnStatus(inv.getID(), true);

			}
		}.runTaskAsynchronously(Donum.getInstance());
	}

	public List<DeathInventory> getDeathInventories(UUID player, int limit) {
		return database.getLastDeathInventories(player, limit);
	}
	
	public DonumDAO getDAO() {
		return database;
	}
}
