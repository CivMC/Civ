package com.github.civcraft.donum.storage;

import com.github.civcraft.donum.Donum;
import java.util.UUID;
import org.bukkit.scheduler.BukkitRunnable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class DatabaseStorage extends IDeliveryStorage {

	public void loadDeliveryInventory(final UUID uuid) {
		new BukkitRunnable() {

			@Override
			public void run() {
				ItemMap im = Donum.getManager().getDAO().getDeliveryInventory(uuid);
				Donum.getManager().setDeliveryInventory(uuid, im);
				postLoad(im, uuid);
			}
		}.runTaskAsynchronously(Donum.getInstance());
	}

	public void updateDeliveryInventory(UUID uuid, ItemMap im, boolean async) {
		if (async) {
			new BukkitRunnable() {

				@Override
				public void run() {
					Donum.getManager().getDAO().updateDeliveryInventory(uuid, im);
				}
			}.runTaskAsynchronously(Donum.getInstance());
		} else {
			Donum.getManager().getDAO().updateDeliveryInventory(uuid, im);
		}
	}
}
