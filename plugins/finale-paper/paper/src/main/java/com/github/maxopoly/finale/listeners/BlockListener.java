package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.CombatTagPlusManager;
import com.github.maxopoly.finale.misc.BlockRestrictionHandler;
import com.github.maxopoly.finale.misc.ally.AllyHandler;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.List;

public class BlockListener implements Listener {



	@EventHandler
	public void onPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		CombatTagPlusManager ctpManager = Finale.getPlugin().getCombatTagPlusManager();
		if (!ctpManager.isTagged(player)) {
			return;
		}

		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		BlockRestrictionHandler.RestrictionMode mode = blockRestrictionHandler.getMode();
		Block blockPlaced = event.getBlockPlaced();
		Material blockPlacedType = blockPlaced.getType();

		if (mode == BlockRestrictionHandler.RestrictionMode.BLACKLIST) {
			List<Material> blacklist = blockRestrictionHandler.getBlacklist();
			if (blacklist.contains(blockPlacedType)) {
				restrictPlacement(event);
				return;
			}
		} else if (mode == BlockRestrictionHandler.RestrictionMode.WHITELIST) {
			List<Material> whitelist = blockRestrictionHandler.getWhitelist();
			if (!whitelist.contains(blockPlacedType)) {
				player.sendMessage(ChatColor.RED + blockPlacedType.toString() + " can't be placed while in combat.");
				event.setCancelled(true);
				return;
			}
		}

		if (blockRestrictionHandler.isOnCooldown(player, blockPlacedType)) {
			restrictPlacement(event);
		} else {
			blockRestrictionHandler.putOnCooldown(player, blockPlacedType);
		}
	}

	private void restrictPlacement(BlockPlaceEvent event) {
		BlockRestrictionHandler blockRestrictionHandler = Finale.getPlugin().getManager().getBlockRestrictionHandler();
		Integer zoneRadius = blockRestrictionHandler.getZoneRadii().get(event.getBlockPlaced().getType());
		if (zoneRadius != null) {
			Collection<LivingEntity> nearbyLivingEntities = event.getBlock().getLocation().getNearbyLivingEntities(zoneRadius);
			boolean enemyNearby = false;
			AllyHandler allyHandler = Finale.getPlugin().getManager().getAllyHandler();
			for (LivingEntity livingEntity : nearbyLivingEntities) {
				if (livingEntity instanceof Player) {
					Player nearbyPlayer = (Player) livingEntity;
					if (!allyHandler.isAllyOf(nearbyPlayer, event.getPlayer())) {
						enemyNearby = true;
						break;
					}
				}
			}
			if (enemyNearby) {
				event.getPlayer().sendMessage(ChatColor.RED + event.getBlockPlaced().getType().toString() +
						" can't be placed within " +
						zoneRadius + "m of a hostile while in combat.");
				event.setCancelled(true);
			}
		} else {
			event.getPlayer().sendMessage(ChatColor.RED + event.getBlockPlaced().getType().toString() + " can't be placed while in combat.");
			event.setCancelled(true);
		}
	}

}
