package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

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
public class Introbook extends SimpleHack<IntrobookConfig> implements Listener, CommandExecutor {

	public static final String NAME = "Introbook";
	private static long bookGiftCount = 0l;

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
		if (config.isEnabled()) {
			plugin().log("Registering introbook command");
			plugin().registerCommand("introbook", this);
		}
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
		Introbook.bookGiftCount = 0l;
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
		    inv.addItem(config.getIntroBook(dead));
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
		    inv.addItem(config.getIntroBook(alive));
		    Introbook.bookGiftCount ++;
		    plugin().debug("Gave {0} an introbook", alive.getName());
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
			sb.append(ChatColor.WHITE).append("\n  Introbooks given out: ");
			sb.append(ChatColor.GREEN).append(Introbook.bookGiftCount);
			sb.append("\n  Current Introbook:");
			ItemStack book = config.getIntroBook(null);
			if (book != null) {
				BookMeta meta = (BookMeta) book.getItemMeta();
				if (meta != null) {
					sb.append(ChatColor.WHITE).append("\n    Title: ")
						.append(ChatColor.AQUA).append(meta.getTitle());
					sb.append(ChatColor.WHITE).append("\n    Author: ")
						.append(ChatColor.AQUA).append(meta.getAuthor());
					sb.append(ChatColor.WHITE).append("\n    Pages: ")
						.append(ChatColor.AQUA).append(meta.getPageCount());
					for (String page : meta.getPages()) {
						sb.append("\n      ").append(page);
					}
				} else {
					sb.append("\n    ").append(ChatColor.RED).append("-- in error --");
				}
			} else {
				sb.append("\n    ").append(ChatColor.RED).append("-- in error --");
			}
		}

		return sb.toString();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) return false;

		Player p = plugin().getServer().getPlayer(args[0]);

		if (p == null) {
			try {
				UUID pu = UUID.fromString(args[0]);
				p = plugin().getServer().getPlayer(pu);
			} catch (IllegalArgumentException iae) {
				p = null;
			}
		}

		if (p == null) {
			sender.sendMessage(ChatColor.RED + "Unable to find " + args[0]);
		} else {
			plugin().log(Level.INFO, "Sent introbook to {0}", args[0]);
			p.sendMessage(ChatColor.GREEN + "You've been given an introductory book!");
			Inventory inv = p.getInventory();
			inv.addItem(config.getIntroBook(p));
		}

		return true;
	}

	public static IntrobookConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new IntrobookConfig(plugin, config);
	}
}
