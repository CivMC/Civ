package com.github.maxopoly.finale.combat.event;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CritHitEvent extends Event {

	private static final HandlerList handlers = new HandlerList(); 
	
	private Player attacker;
	private LivingEntity victim;
	private float critMultiplier;
	
	public CritHitEvent(Player attacker, LivingEntity victim, float critMultiplier) {
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
	
	public float getCritMultiplier() {
		return critMultiplier;
	}
	
	public void setCritMultiplier(float set) {
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
