package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Apparently if you open a new inventory during a click event, the exact same
 * event is called again on the new inventory. To be able to open new
 * inventories from existing ones, we need to schedule the opening instead
 *
 */
public class ScheduledInventoryOpen implements Runnable {

	private ClickableInventory ci;
	private Player p;

	public ScheduledInventoryOpen(ClickableInventory ci, Player p) {
		this.ci = ci;
		this.p = p;
	}
	public void run() {
		ci.showInventory(p);
	}

	public static void schedule(JavaPlugin plugin, ClickableInventory ci, Player p) {
		ScheduledInventoryOpen sio = new ScheduledInventoryOpen(ci, p);
		Bukkit.getServer().getScheduler().runTask(plugin, sio);
	}
}
