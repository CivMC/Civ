package com.github.igotyou.FactoryMod.listeners;

import com.github.igotyou.FactoryMod.events.FactoryActivateEvent;
import com.github.igotyou.FactoryMod.factories.FurnCraftChestFactory;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.compaction.Compaction;

/**
 * Used to handle events related to items with compacted lore
 */
public final class CompactItemListener implements Listener {
	/**
	 * Prevents players from wordbanking compacted items
	 */
	@EventHandler
	private void preventWordBankIfCompacted(
		final @NotNull FactoryActivateEvent event
	) {
		final var factory = ((FurnCraftChestFactory) event.getFactory());
		if (!factory.getCurrentRecipe().getTypeIdentifier().equals("WORDBANK")) {
			return;
		}
		if (Compaction.isCompacted(factory.getInputInventory().getItem(0))) {
			event.setCancelled(true);
			event.getActivator().sendMessage(ChatColor.RED + "You cannot wordbank compacted items");
		}
	}
}
