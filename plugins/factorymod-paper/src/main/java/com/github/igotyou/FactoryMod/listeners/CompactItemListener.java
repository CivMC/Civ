package com.github.igotyou.FactoryMod.listeners;

import com.github.igotyou.FactoryMod.FactoryMod;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Used to handle events related to items with compacted lore
 *
 */
public class CompactItemListener implements Listener {

	/**
	 * Prevents players from placing compacted blocks
	 */
	@EventHandler
	public void blockPlaceEvent(BlockPlaceEvent e) {
		if (isCompacted(e.getItemInHand())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You can not place compacted blocks");
		}

	}

	/**
	 * Prevents players from crafting with compacted materials
	 */
	@EventHandler
	public void craftingEvent(CraftItemEvent e) {
		CraftingInventory ci = e.getInventory();
		for (ItemStack is : ci.getMatrix()) {
			if (isCompacted(is)) {
				e.setCancelled(true);
				HumanEntity h = e.getWhoClicked();
				if (h instanceof Player) {
					((Player) h)
							.sendMessage(ChatColor.RED + "You can not craft with compacted items");
				}
				break;
			}
		}
	}

	/**
	 * Prevents players from eating compacted items
	 */
	@EventHandler
	public void itemConsumeEvent(PlayerItemConsumeEvent e) {
		if (isCompacted(e.getItem())) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(ChatColor.RED + "You can not eat compacted food");
		}

	}

	private boolean isCompacted(ItemStack is) {
		if (is == null) {
			return false;
		}
		if (!is.hasItemMeta()) {
			return false;
		}
		ItemMeta im = is.getItemMeta();
		if (!im.hasLore()) {
			return false;
		}
		for(String lore : im.getLore()) {
			if (FactoryMod.getInstance().getManager().isCompactLore(lore)) {
				return true;
			}
		}
		return false;
	}

}
