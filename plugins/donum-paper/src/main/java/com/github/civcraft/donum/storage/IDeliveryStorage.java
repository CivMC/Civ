package com.github.civcraft.donum.storage;

import com.github.civcraft.donum.Donum;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;


public abstract class IDeliveryStorage {
	
	public abstract void loadDeliveryInventory(UUID uuid);
	
	public abstract void updateDeliveryInventory(UUID uuid, ItemMap im, boolean async);
	
	public void postLoad(ItemMap delivery, UUID uuid) {
		if (delivery.getTotalItemAmount() != 0) {
			Player p = Bukkit.getPlayer(uuid);
			if (p != null) {
				p.sendMessage(ChatColor.GOLD + "You have " + delivery.getTotalItemAmount()
						+ " items available to claim! Run /present to open your delivery inventory");
			}
		}
		Donum.getInstance().debug("Loaded " + delivery.toString() + " for " + uuid.toString());
	}
}
