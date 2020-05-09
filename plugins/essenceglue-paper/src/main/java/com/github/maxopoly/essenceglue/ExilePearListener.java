package com.github.maxopoly.essenceglue;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.devotedmc.ExilePearl.event.PearlDecayEvent;

public class ExilePearListener implements Listener {
	
	private boolean multiplyCost;
	private StreakManager streakMan;
	
	public ExilePearListener(StreakManager streakMan, boolean multiplyCost) {
		this.multiplyCost = multiplyCost;
		this.streakMan = streakMan;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void pearlDecay(PearlDecayEvent event) {
		if (!multiplyCost) {
			return;
		}
		int streak = streakMan.getCurrentStreak(event.getPearl().getPlayerId(), false);
		event.setDamageAmount(event.getDamageAmount() * streak);
	}

}
