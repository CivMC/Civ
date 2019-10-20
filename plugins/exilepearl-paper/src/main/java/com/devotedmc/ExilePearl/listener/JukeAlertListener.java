package com.devotedmc.ExilePearl.listener;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.ExileRule;

public class JukeAlertListener extends RuleListener {


	public JukeAlertListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	/**
	 * Prevents exiled players from placing snitches
	 * @param e The event
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onSnitchPlaced(BlockPlaceEvent e) {
		Material m = e.getBlockPlaced().getType();
		if (m == Material.JUKEBOX || m == Material.NOTE_BLOCK) {
			checkAndCancelRule(ExileRule.SNITCH, e, e.getPlayer());
		}
	}
}
