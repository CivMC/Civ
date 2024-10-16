package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.crossbow.AntiAirMissile;
import com.github.maxopoly.finale.misc.crossbow.CrossbowHandler;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class CrossbowListener implements Listener {

	private Set<UUID> firedFireworks = new HashSet<>();

	@EventHandler
	public void onShootCrossbow(EntityShootBowEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		Player shooter = (Player) event.getEntity();
		ItemStack bow = event.getBow();
		if (bow.getType() != Material.CROSSBOW) {
			return;
		}

		CrossbowHandler crossbowHandler = Finale.getPlugin().getManager().getCrossbowHandler();
		if (crossbowHandler.onCooldown(shooter)) {
			event.setCancelled(true);
			event.setConsumeItem(false);
			return;
		}

		ItemStack consumable = event.getConsumable();
		if (consumable.getType() == Material.FIREWORK_ROCKET) {
			firedFireworks.add(event.getProjectile().getUniqueId());
			crossbowHandler.putOnCooldown(shooter);
			return;
		}

		if (consumable.getType() == Material.TIPPED_ARROW) {
			AntiAirMissile antiAirMissile = crossbowHandler.getAntiAirMissile(consumable);
			if (antiAirMissile == null) {
				return;
			}

			event.setCancelled(true);
			antiAirMissile.fireAAMissile(shooter);
			crossbowHandler.putOnCooldown(shooter);
		}
	}

	@EventHandler
	public void onFireworkExplode(FireworkExplodeEvent event) {
		CrossbowHandler crossbowHandler = Finale.getPlugin().getManager().getCrossbowHandler();
		if (firedFireworks.contains(event.getEntity().getUniqueId())) {
			crossbowHandler.handleFireworkExplode(event);
		}
	}

}
