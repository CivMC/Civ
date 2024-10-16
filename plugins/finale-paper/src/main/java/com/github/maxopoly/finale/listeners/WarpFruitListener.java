package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.misc.warpfruit.WarpFruitTracker;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WarpFruitListener implements Listener {

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		WarpFruitTracker warpFruitTracker = Finale.getPlugin().getManager().getWarpFruitTracker();
		Player player = event.getPlayer();
		warpFruitTracker.quit(player);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		WarpFruitTracker warpFruitTracker = Finale.getPlugin().getManager().getWarpFruitTracker();
		warpFruitTracker.logLocation(event.getPlayer());
	}

	private boolean isChorusHolder(Player player) {
		ItemStack mainItem = player.getInventory().getItemInMainHand();
		ItemStack offItem = player.getInventory().getItemInOffHand();
		return (mainItem != null && mainItem.getType() == Material.CHORUS_FRUIT) || (offItem != null && offItem.getType() == Material.CHORUS_FRUIT);
	}

	@EventHandler
	public void onEatingWarpFruit(PlayerInteractEvent event) {
		WarpFruitTracker warpFruitTracker = Finale.getPlugin().getManager().getWarpFruitTracker();
		Player player = event.getPlayer();
		if (warpFruitTracker.onCooldown(player)) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (!isChorusHolder(player)) {
				return;
			}
			new BukkitRunnable() {

				@Override
				public void run() {
					if (!player.isOnline() || player.isDead()) {
						if (warpFruitTracker.isSpectralWhileChanneling() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
							player.removePotionEffect(PotionEffectType.GLOWING);
						}
						cancel();
						return;
					}
					if (player.getItemInUse() == null){
						if (warpFruitTracker.isSpectralWhileChanneling() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
							player.removePotionEffect(PotionEffectType.GLOWING);
						}
						cancel();
						return;
					}
					if (player.getItemInUse().getType() != Material.CHORUS_FRUIT) {
						if (warpFruitTracker.isSpectralWhileChanneling() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
							player.removePotionEffect(PotionEffectType.GLOWING);
						}
						cancel();
						return;
					}
					if (warpFruitTracker.onCooldown(player)) {
						if (warpFruitTracker.isSpectralWhileChanneling() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
							player.removePotionEffect(PotionEffectType.GLOWING);
						}
						cancel();
						return;
					}
					if (warpFruitTracker.isSpectralWhileChanneling() && !player.hasPotionEffect(PotionEffectType.GLOWING)) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0));
					}
					warpFruitTracker.animate(player);
				}
			}.runTaskTimer(Finale.getPlugin(), 0L,1L);
		}
	}

	@EventHandler
	public void onEatWarpFruit(PlayerItemConsumeEvent event) {
		WarpFruitTracker warpFruitTracker = Finale.getPlugin().getManager().getWarpFruitTracker();
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItem();
		if (itemStack.getType() == Material.CHORUS_FRUIT) {
			if (warpFruitTracker.timewarp(player)) {
				if (warpFruitTracker.isSpectralWhileChanneling() && player.hasPotionEffect(PotionEffectType.GLOWING)) {
					player.removePotionEffect(PotionEffectType.GLOWING);
				}
				event.setCancelled(true);
			}
		}
	}

//	@EventHandler
//	public void onVanillaChorusTeleport(PlayerTeleportEvent event) {
//		WarpFruitTracker warpFruitTracker = Finale.getPlugin().getManager().getWarpFruitTracker();
//		Player player = event.getPlayer();
//		if (!warpFruitTracker.onCooldown(player) && event.getCause() == PlayerTeleportEvent.TeleportCause.CHORUS_FRUIT) {
//			event.setCancelled(true);
//		}
//	}

}
