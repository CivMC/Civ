package com.github.maxopoly.finale.listeners;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.FinaleManager;

public class PlayerListener implements Listener {

	private FinaleManager manager;

	public PlayerListener(FinaleManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if (manager.isAttackSpeedEnabled()) {;
			// Set attack speed
			AttributeInstance attr = e.getPlayer().getAttribute(
				Attribute.GENERIC_ATTACK_SPEED);
			if (attr != null) {
				attr.setBaseValue(manager.getAttackSpeed());
			}
		}
		if (manager.isRegenHandlerEnabled()) {
			// Register login for custom health regen
			manager.getPassiveRegenHandler().registerPlayer(
					e.getPlayer().getUniqueId());
		}
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent e) {
		if (!manager.isRegenHandlerEnabled()) return;
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if (e.getRegainReason() == RegainReason.SATIATED
				&& manager.getPassiveRegenHandler().blockPassiveHealthRegen()) {
			// apparently setting to cancelled doesn't prevent the "consumption" of satiation.
			Player p = (Player) e.getEntity();
			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			float newExhaustion = (float) (p.getExhaustion() - e.getAmount() * 6.0d);

			if(newExhaustion < 0)
				newExhaustion = 0;

			p.setExhaustion(newExhaustion);
			StringBuffer alterHealth = new StringBuffer(p.getName());
			alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
			alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
			alterHealth.append(":").append(p.getFoodLevel());
			Finale.getPlugin().getLogger().info(alterHealth.toString());
			e.setCancelled(true);
			return;
		}
		if (e.getRegainReason() == RegainReason.EATING && manager.getPassiveRegenHandler().blockFoodHealthRegen()) {
			Player p = (Player) e.getEntity();
			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			StringBuffer alterHealth = new StringBuffer("EATING:" + p.getName());
			alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
			alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
			alterHealth.append(":").append(p.getFoodLevel());
			Finale.getPlugin().getLogger().info(alterHealth.toString());
			e.setCancelled(true);
		}
	}
	
	//@EventHandler
	public void arrowHit(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		if (e.getDamager().getType() == EntityType.TIPPED_ARROW) {
			return;
		}
	}

}
