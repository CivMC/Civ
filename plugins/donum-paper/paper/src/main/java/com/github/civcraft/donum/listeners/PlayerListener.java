package com.github.civcraft.donum.listeners;

import com.github.civcraft.donum.Donum;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import vg.civcraft.mc.civmodcore.inventory.items.ItemMap;

public class PlayerListener implements Listener {
	
	@EventHandler
	public void playerWasStupid(PlayerDeathEvent e) {
		Donum.getManager().saveDeathInventory(e.getEntity().getUniqueId(), new ItemMap(e.getDrops()));
	}
}
