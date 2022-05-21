package com.github.civcraft.donum.listeners;

import com.github.civcraft.donum.Donum;
import com.github.civcraft.donum.DonumAPI;
import com.github.civcraft.donum.gui.AdminDeliveryGUI;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;
import vg.civcraft.mc.namelayer.NameAPI;

public class AdminDeliveryListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void closeInventory(InventoryCloseEvent e) {
		if (AdminDeliveryGUI.isEditingPlayerDeliveryInventory(e.getPlayer().getUniqueId())) {
			ItemMap im = new ItemMap();
			for(ItemStack is : e.getInventory().getContents()) {
				if (is == null  || is.getType() == Material.AIR) {
					continue;
				}
				im.addItemStack(is);
			}
			UUID beingViewed=AdminDeliveryGUI.getPlayerBeingViewed(e.getPlayer().getUniqueId());
			AdminDeliveryGUI.closedInventory(e.getPlayer().getUniqueId());
			DonumAPI.deliverItem(beingViewed, im);
			Donum.getInstance().info(e.getPlayer().getName() + " added " + im.getTotalItemAmount() + " items to delivery inventory of " + NameAPI.getCurrentName(beingViewed));
			e.getPlayer().sendMessage(ChatColor.GREEN + "Successfully added " + im.getTotalItemAmount() + " items to delivery inventory of " + NameAPI.getCurrentName(beingViewed));
		}
	}
}
