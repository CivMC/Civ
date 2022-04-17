package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.TridentHandler;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TridentListener implements Listener {

	private Map<UUID, Trident> returnToOffhand = new HashMap<>();

	@EventHandler
	public void onTridentThrow(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof Trident)) {
			return;
		}

		Trident trident = (Trident) event.getEntity();
		if (!(trident.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) trident.getShooter();
		TridentHandler tridentHandler = Finale.getPlugin().getManager().getTridentHandler();
		ItemStack mainhand = shooter.getInventory().getItemInMainHand();
		ItemStack offhand = shooter.getInventory().getItemInOffHand();
		boolean mainhandTrident = mainhand != null && mainhand.getType() == Material.TRIDENT;
		boolean offhandTrident = offhand != null && offhand.getType() == Material.TRIDENT;
		if (mainhandTrident || offhandTrident) {
			ItemStack tridentItem = trident.getItemStack();
			boolean hasRiptide = tridentItem.getItemMeta().hasEnchant(Enchantment.RIPTIDE);
			if (hasRiptide) {
				if (tridentHandler.isRiptideOnCooldown(shooter)) {
					event.setCancelled(true);
					return;
				}
			}
			if (tridentHandler.isTridentOnCooldown(shooter)) {
				event.setCancelled(true);
				return;
			} else {
				tridentHandler.putTridentOnCooldown(shooter);
			}

			if (offhandTrident && !mainhandTrident) {
				if (tridentHandler.isReturnToOffhand()) {
					returnToOffhand.put(shooter.getUniqueId(), trident);
				}
			}
		}
	}

	@EventHandler
	public void onTridentPickup(PlayerPickupArrowEvent event) {
		if (!(event.getArrow() instanceof Trident)) {
			return;
		}

		Trident trident = (Trident) event.getArrow();
		if (!(trident.getShooter() instanceof Player)) {
			return;
		}

		Player shooter = (Player) trident.getShooter();
		Trident returnTrident = returnToOffhand.get(shooter.getUniqueId());
		if (returnTrident == null) {
			return;
		}
		if (returnTrident.getUniqueId().equals(trident.getUniqueId())) {
			ItemStack offhand = shooter.getInventory().getItemInOffHand();
			System.out.println(offhand);
			if (offhand == null || offhand.getType().isAir()) {
				trident.remove();
				event.setCancelled(true);
				shooter.getInventory().setItemInOffHand(trident.getItemStack());
			}
		}
	}

	@EventHandler
	public void onSurfaceRiptide(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		ItemStack mainhand = player.getInventory().getItemInMainHand();
		ItemStack offhand = player.getInventory().getItemInOffHand();
		boolean mainhandRiptideTrident = mainhand != null && mainhand.getType() == Material.TRIDENT && mainhand.getItemMeta().hasEnchant(Enchantment.RIPTIDE);
		boolean offhandRiptideTrident = offhand != null && offhand.getType() == Material.TRIDENT && offhand.getItemMeta().hasEnchant(Enchantment.RIPTIDE);;
		if (player.isHandRaised() && (mainhandRiptideTrident || offhandRiptideTrident)) {
			TridentHandler tridentHandler = Finale.getPlugin().getManager().getTridentHandler();
			if (tridentHandler.isRiptideOnCooldown(player)) {
				System.out.println("STOP SURFACE RIPTIDE");
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onRiptide(PlayerRiptideEvent event) {
		Player player = event.getPlayer();
		TridentHandler tridentHandler = Finale.getPlugin().getManager().getTridentHandler();
		if (tridentHandler.isRiptideOnCooldown(player)) {
			Location oldLoc = player.getLocation();
			new BukkitRunnable() {

				@Override
				public void run() {
					player.teleport(oldLoc);
				}

			}.runTaskLater(Finale.getPlugin(), 1);
			return;
		}
		tridentHandler.putRiptideOnCooldown(player);
	}

}
