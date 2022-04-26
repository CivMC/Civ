package com.github.maxopoly.essenceglue;

import com.devotedmc.ExilePearl.event.PearlDecayEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ExilePearListener implements Listener {

	private final boolean multiplyCost;
	private final StreakManager streakMan;

	public ExilePearListener(StreakManager streakMan, boolean multiplyCost) {
		this.multiplyCost = multiplyCost;
		this.streakMan = streakMan;
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void pearlDecay(PearlDecayEvent event) {
		if (!multiplyCost) {
			return;
		}
		int streak = streakMan.getRecalculatedCurrentStreak(event.getPearl().getPlayerId());
		event.setDamageAmount(event.getDamageAmount() * streak);
	}

}
