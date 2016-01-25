package com.github.igotyou.FactoryMod.utility;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import vg.civcraft.mc.civmodcore.itemHandling.ItemMap;

import com.github.igotyou.FactoryMod.FactoryMod;

public class LoggingUtils {

	public static void log(String msg) {
		FactoryMod.getPlugin().getLogger().log(Level.INFO, msg);
	}

	private static String serializeInventory(Inventory i) {
		return new ItemMap(i).toString();
	}

	public static void logInventory(Block b) {
		if (FactoryMod.getManager().logInventories()
				&& b.getState() instanceof InventoryHolder) {
			log("Contents of "
					+ b.getType().toString()
					+ " at "
					+ b.getLocation().toString()
					+ " contains: "
					+ serializeInventory(((InventoryHolder) b.getState())
							.getInventory()));
		}
	}

	public static void logInventory(Inventory i, String msg) {
		if (FactoryMod.getManager().logInventories()) {
			log(msg + serializeInventory(i));
		}
	}
}
