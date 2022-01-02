package com.github.maxopoly.finale.combat.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CritHitEvent extends Event {

	private static final HandlerList handlers = new HandlerList(); 
	
	private Player attacker;
	private LivingEntity victim;
	private double critMultiplier;
	
	public CritHitEvent(Player attacker, LivingEntity victim, double critMultiplier) {
		this.attacker = attacker;
		this.victim = victim;
		this.critMultiplier = critMultiplier;
	}
	
	public Player getAttacker() {
		return attacker;
	}
	
	public LivingEntity getVictim() {
		return victim;
	}
	
	public double getCritMultiplier() {
		return critMultiplier;
	}
	
	public void setCritMultiplier(double set) {
		this.critMultiplier = set;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
}
