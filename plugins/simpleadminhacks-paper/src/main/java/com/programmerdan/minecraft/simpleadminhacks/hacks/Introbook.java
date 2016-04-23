package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.IntrobookConfig;

/**
 * Simple Hack that handles gifting introbooks if enabled. Handles respawns as well
 * through first restart, effectively.
 * 
 * @author ProgrammerDan <programmerdan@gmail.com
 *
 */
public class Introbook extends SimpleHack<IntrobookConfig> implements Listener {

	public static final String NAME = "Introbook";
	
	private Set<UUID> hasBook;
	
	public Introbook(SimpleAdminHacks plugin, IntrobookConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering various Introbook listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
	}

	@Override
	public void dataBootstrap() {
		hasBook = new HashSet<UUID>();
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
		this.hasBook.clear();
		this.hasBook = null;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void determineBookDrop(PlayerDeathEvent event) {
		if (!config.isEnabled() || !config.doesFollow()) return;
		
		Player dead = event.getEntity();
		if (dead == null) return;
		
		UUID puid = dead.getUniqueId();
		if (puid == null) return;
		
		// Check if they have the book on them.
		List<ItemStack> toDrop = event.getDrops();
		for (int idx = 0; idx < toDrop.size(); idx++) {
			if (config.isIntroBook(toDrop.get(idx))) {
				this.hasBook.add(puid);
				toDrop.remove(idx);
				return; // found it, so don't drop it yet
			}
		}
		this.hasBook.remove(puid);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void determineBookRespawn(PlayerRespawnEvent event) {
		if (!config.isEnabled() || !config.doesFollow()) return;
		
		Player dead = event.getPlayer();
		if (dead == null) return;
		
		UUID puid = dead.getUniqueId();
		if (puid == null) return;
		
		if (this.hasBook.remove(puid)) {
		    Inventory inv = dead.getInventory();
		    inv.addItem(config.getIntroBook());
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void determineBookJoin(PlayerJoinEvent event) {
		if (!config.isEnabled()) return;

		Player alive = event.getPlayer();
		if (alive == null) return;
		
		UUID puid = alive.getUniqueId();
		if (puid == null) return;
		
		if (!alive.hasPlayedBefore() || this.hasBook.contains(puid)) {
			this.hasBook.remove(puid);
		    Inventory inv = alive.getInventory();
		    inv.addItem(config.getIntroBook());
		}
	}

	@Override
	public String status() {
		StringBuffer sb = new StringBuffer();
		if (config != null && config.isEnabled()) {
			sb.append("Introbook monitoring active");
		} else {
			sb.append("Introbook monitoring not active");
		}
		if (config.isEnabled()) {
			sb.append("\n  Current Introbook:");
			ItemStack book = config.getIntroBook();
			if (book != null) {
				sb.append("\n    ").append(book);
			} else {
				sb.append("\n    ").append(ChatColor.RED).append("-- in error --");
			}
		}
		
		return sb.toString();
	}

}
