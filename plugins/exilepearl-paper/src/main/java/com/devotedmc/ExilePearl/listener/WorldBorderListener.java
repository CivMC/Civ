package com.devotedmc.ExilePearl.listener;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.devotedmc.ExilePearl.ExilePearl;
import com.devotedmc.ExilePearl.ExilePearlApi;
import com.devotedmc.ExilePearl.PearlFreeReason;
import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import com.devotedmc.ExilePearl.event.PearlDecayEvent.DecayAction;

public class WorldBorderListener extends RuleListener {

	private HashSet<ExilePearl> toFree = new HashSet<ExilePearl>();
	
	public WorldBorderListener(ExilePearlApi pearlApi) {
		super(pearlApi);
	}


	/**
	 * Frees any pearls that are stored beyond world border
	 * @param e The event args
	 */
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onPearlDecay(PearlDecayEvent e) {
		boolean autoFree = pearlApi.getPearlConfig().getShouldAutoFreeWorldBorder();
		
		if (!autoFree || e.getAction() != DecayAction.COMPLETE) {
			return;
		}
		
		toFree.clear();
		
		// Free any pearls outside world border
		for (ExilePearl pearl : pearlApi.getPearls()) {
			Location l = pearl.getLocation();
			if(!pearlApi.isLocationInsideBorder(l)) {
				toFree.add(pearl);
			}
		}
		
		for (ExilePearl pearl : toFree) {
			pearlApi.freePearl(pearl, PearlFreeReason.OUTSIDE_WORLD_BORDER);
		}
	}
}
