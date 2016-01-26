package com.github.igotyou.FactoryMod.listeners;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

/**
 * Used to handle events related to items with compacted lore
 *
 */
public class CompactItemListener implements Listener {
	private String compactLore;

	public CompactItemListener(String compactLore) {
		this.compactLore = compactLore;
	}

	/**
	 * Prevents players from placing compacted blocks
	 */
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e) {
		if (!e.getItemInHand().hasItemMeta()) {
			return;
		}
		if (!e.getItemInHand().getItemMeta().hasLore()) {
			return;
		}
		if (e.getItemInHand().getItemMeta().getLore().get(0)
				.equals(compactLore)) {
			e.setCancelled(true);
			Player p = e.getPlayer();
			if (p != null) {
				p.sendMessage("You can not place compacted blocks");
			}
		}

	}

	/**
	 * Prevents players from crafting with compacted materials
	 */
	@EventHandler
	public void craftingEvent(CraftItemEvent e) {
		CraftingInventory ci = e.getInventory();
		for (ItemStack is : ci.getMatrix()) {
			if (is == null) {
				continue;
			}
			if (!is.hasItemMeta()) {
				continue;
			}
			if (!is.getItemMeta().hasLore()) {
				continue;
			}
			if (is.getItemMeta().getLore().get(0).equals(compactLore)) {
				e.setCancelled(true);
				HumanEntity h = e.getWhoClicked();
				if (h instanceof Player && h != null) {
					((Player) h)
							.sendMessage("You can not craft with compacted items");
				}
				break;
			}
		}
	}

}
