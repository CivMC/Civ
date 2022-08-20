package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.GappleHandler;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class GappleListener implements Listener {

	@EventHandler
	public void onEatGapple(PlayerItemConsumeEvent event) {
		ItemStack itemStack = event.getItem();
		if (itemStack.getType() != Material.GOLDEN_APPLE) {
			return;
		}

		GappleHandler gappleHandler = Finale.getPlugin().getManager().getGappleHandler();
		if (gappleHandler.onCooldown(event.getPlayer())) {
			event.setCancelled(true);
			return;
		}

		gappleHandler.putOnCooldown(event.getPlayer());
	}

}
