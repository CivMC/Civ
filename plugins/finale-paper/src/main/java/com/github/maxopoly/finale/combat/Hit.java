package com.github.maxopoly.finale.combat;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Hit {

	private Player attacker;
	private LivingEntity victim;
	
	public Hit(Player attacker, LivingEntity victim) {
		this.attacker = attacker;
		this.victim = victim;
	}
	
	public Player getAttacker() {
		return attacker;
	}
	
	public LivingEntity getVictim() {
		return victim;
	}
}
