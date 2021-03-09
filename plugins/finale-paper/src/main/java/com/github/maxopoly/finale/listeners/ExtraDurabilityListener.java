package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ArmourModifier;
import com.github.maxopoly.finale.misc.ExtraDurabilityTracker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

public class ExtraDurabilityListener implements Listener {

	@EventHandler
	public void onDurabilityChange(PlayerItemDamageEvent e) {
		ItemStack is = e.getItem();
		ArmourModifier armourModifier = Finale.getPlugin().getManager().getArmourModifier();
		ExtraDurabilityTracker extraDurabilityTracker = armourModifier.getExtraDurabilityTracker();
		boolean reduce = extraDurabilityTracker.reduceDurability(e.getPlayer(), is.getType());
		e.setCancelled(!reduce);
	}

}
