package com.github.maxopoly.finale.listeners;

import com.github.maxopoly.finale.Finale;
import com.github.maxopoly.finale.external.CombatTagPlusManager;
import com.github.maxopoly.finale.misc.BlockRestrictionHandler;
import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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
				player.sendMessage(ChatColor.RED + blockPlacedType.toString() + " can't be placed while in combat.");
				event.setCancelled(true);
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
			player.sendMessage(ChatColor.RED + blockPlacedType.toString() + " can't be placed while in combat.");
			event.setCancelled(true);
		} else {
			blockRestrictionHandler.putOnCooldown(player, blockPlacedType);
		}
	}

}
