package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.ShieldHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ShieldListener implements Listener {

	private Set<UUID> hasPassiveResistance = new HashSet<>();

	@EventHandler
	public void onShieldBash(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			//System.out.println("RIGHT CLICKING");
			ItemStack item = event.getItem();
			if (item == null || item.getType() != Material.SHIELD) {
				return;
			}
			//System.out.println("RIGHT CLICKING ON SHIELD");

			Player player = event.getPlayer();
			new BukkitRunnable() {

				@Override
				public void run() {
					if (player.isBlocking()) {
						//System.out.println("BLOCKING");
						ShieldHandler shieldHandler = Finale.getPlugin().getManager().getShieldHandler();
						shieldHandler.activateShieldBash(player);
						cancel();
						return;
					}
					if (player.isHandRaised()) {
						//System.out.println("HAND RAISED");
						return;
					}
					cancel();
				}

			}.runTaskTimer(Finale.getPlugin(), 1L, 1L);
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		ShieldHandler shieldHandler = Finale.getPlugin().getManager().getShieldHandler();
		if (!shieldHandler.isPassiveResistanceEnabled()) {
			return;
		}

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		ItemStack cursor = event.getCursor();
		if (cursor.getType() != Material.SHIELD) {
			return;
		}

		passiveResistanceCheck(((Player) event.getWhoClicked()).getPlayer());
	}

	@EventHandler
	public void onSlotSwitch(PlayerItemHeldEvent event) {
		ShieldHandler shieldHandler = Finale.getPlugin().getManager().getShieldHandler();
		if (shieldHandler.isPassiveResistanceEnabled()) {
			passiveResistanceCheck(event.getPlayer());
		}
	}

	@EventHandler
	public void onShieldSwap(PlayerSwapHandItemsEvent event) {
		ShieldHandler shieldHandler = Finale.getPlugin().getManager().getShieldHandler();
		if (shieldHandler.isPassiveResistanceEnabled()) {
			passiveResistanceCheck(event.getPlayer());
		}
	}

	private void passiveResistanceCheck(Player player) {
		new BukkitRunnable() {

			@Override
			public void run() {
				ItemStack offHand = player.getInventory().getItemInOffHand();
				ItemStack mainHand = player.getInventory().getItemInMainHand();
				if (offHand != null && offHand.getType() == Material.SHIELD) {
					addPassiveResistance(player);
				} else if (mainHand != null && mainHand.getType() == Material.SHIELD) {
					addPassiveResistance(player);
				} else {
					if (hasPassiveResistance.contains(player.getUniqueId())) {
						player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
						hasPassiveResistance.remove(player.getUniqueId());
					}
				}
			}

		}.runTask(Finale.getPlugin());
	}

	private void addPassiveResistance(Player player) {
		ShieldHandler shieldHandler = Finale.getPlugin().getManager().getShieldHandler();
		if (player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			return;
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, shieldHandler.getPassiveResistanceAmplifier()));
		hasPassiveResistance.add(player.getUniqueId());
	}

}
