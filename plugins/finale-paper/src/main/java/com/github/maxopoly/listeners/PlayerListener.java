package com.github.maxopoly.listeners;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.maxopoly.FinaleManager;

public class PlayerListener implements Listener {
	
	private FinaleManager manager;
	
	public PlayerListener(FinaleManager manager) {
		this.manager = manager;
	}
	
	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		System.out.println("Settign");
		AttributeInstance attr = e.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		if (attr != null) {
			System.out.println(manager.getAttackSpeed());
			attr.setBaseValue(manager.getAttackSpeed());
		}
	}
	
	@EventHandler
	public void damagePlayer(EntityDamageByEntityEvent e) {
		
	}
	
	
	
	
}
