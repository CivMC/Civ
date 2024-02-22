package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.FinaleManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

	private FinaleManager manager;

	public PlayerListener(FinaleManager manager) {
		this.manager = manager;
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (manager.isAttackSpeedEnabled()) {
				fixAttackSpeed(p);
			}
			if (manager.isRegenHandlerEnabled()) {
				// Register login for custom health regen
				manager.getPassiveRegenHandler().registerPlayer(p.getUniqueId());
			}
		}
	}

	@EventHandler
	public void damageEntity(EntityDamageEvent e) {
		if (!manager.isInvulTicksEnabled()){
			return;
		}
		if (!(e.getEntity() instanceof LivingEntity)) {
			return;
		}
		LivingEntity victim = (LivingEntity) e.getEntity();
		if (victim.getNoDamageTicks() > 0) {
			e.setCancelled(true);
			return;
		}
		// see
		// https://bukkit.org/threads/whats-up-with-setnodamageticks.141901/#post-1638021
		Bukkit.getScheduler().scheduleSyncDelayedTask(Finale.getPlugin(), new Runnable() {

			@Override
			public void run() {
				Integer ticks = manager.getInvulnerableTicks().get(e.getCause());
				if (ticks == null) {
					return;
				}
				victim.setNoDamageTicks(ticks - 1);
			}
			
		}, 1L);
	}

	@EventHandler
	public void healthRegen(EntityRegainHealthEvent e) {
		if (!manager.isRegenHandlerEnabled()) {
			return;
		}
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if (e.getRegainReason() == RegainReason.SATIATED
				&& manager.getPassiveRegenHandler().blockPassiveHealthRegen()) {
			// apparently setting to cancelled doesn't prevent the "consumption" of
			// satiation.
			Player p = (Player) e.getEntity();

			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			@SuppressWarnings("resource")
			double spigotRegenExhaustion = ((CraftWorld) p
					.getWorld()).getHandle().spigotConfig.regenExhaustion;
			float newExhaustion = (float) (p.getExhaustion() - e.getAmount() * spigotRegenExhaustion);

			StringBuilder alterHealth = null;
			if (manager.isDebug()) {
				alterHealth = new StringBuilder("SATIATED: " + p.getName());
				alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
			}
			if (newExhaustion < 0) // not 100% sure this is correct route; intention was restoring what spigot
									// takes, but we'll roll with it
				newExhaustion = 0;

			p.setExhaustion(newExhaustion);

			if (manager.isDebug()) {
				alterHealth.append(" TO ").append(p.getHealth()).append("<").append(p.getMaxHealth());
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
				Finale.getPlugin().getLogger().info(alterHealth.toString());
			}
			e.setCancelled(true);
			return;
		}
		if (e.getRegainReason() == RegainReason.EATING && manager.getPassiveRegenHandler().blockFoodHealthRegen()) {
			Player p = (Player) e.getEntity();
			double maxHealth = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
			if (manager.isDebug()) {
				StringBuilder alterHealth = new StringBuilder("EATING:" + p.getName());
				alterHealth.append(":").append(p.getHealth()).append("<").append(maxHealth);
				alterHealth.append(":").append(p.getSaturation()).append(":").append(p.getExhaustion());
				alterHealth.append(":").append(p.getFoodLevel());
				Finale.getPlugin().getLogger().info(alterHealth.toString());
			}
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void playerLogin(PlayerJoinEvent e) {
		if (manager.isAttackSpeedEnabled()) {
			fixAttackSpeed(e.getPlayer());
		}
		if (manager.isRegenHandlerEnabled()) {
			// Register login for custom health regen
			manager.getPassiveRegenHandler().registerPlayer(e.getPlayer().getUniqueId());
		}
		if (Finale.getPlugin().getCombatTagPlusManager() == null) {
			return;
		}
		if (manager.getCTPOnLogin()) {
			Finale.getPlugin().getCombatTagPlusManager().tag(e.getPlayer(), null);
			e.getPlayer().sendMessage(ChatColor.RED + "You have been combat tagged on login");
		}
	}

	private void fixAttackSpeed(Player p) {
		AttributeInstance attr = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
		if (attr != null) {
			attr.setBaseValue(manager.getAttackSpeed());
		}
	}

}
