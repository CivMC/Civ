package com.programmerdan.minecraft.simpleadminhacks.hacks;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ExperimentalConfig;

public class Experimental extends SimpleHack<ExperimentalConfig> implements Listener, CommandExecutor {

	public static final String NAME = "Experimental";

	public Experimental(SimpleAdminHacks plugin, ExperimentalConfig config) {
		super(plugin, config);
	}

	@Override
	public void registerListeners() {
		if (config.isEnabled()) {
			plugin().log("Registering experimental listeners");
			plugin().registerListener(this);
		}
	}

	@Override
	public void registerCommands() {
		if (config.isEnabled()) {
			plugin().log("Registering experimental commands");
			plugin().registerCommand("serialize", this);
		}
	}

	@Override
	public void dataBootstrap() {
	}

	@Override
	public void unregisterListeners() {
	}

	@Override
	public void unregisterCommands() {
	}

	@Override
	public void dataCleanup() {
	}

	@Override
	public String status() {
		if (config.isEnabled()) {
			StringBuilder sb = new StringBuilder("Experiments enabled:");
			if (config.isCombatSpy()) {
				sb.append("\n  CombatSpy is on");
			} else {
				sb.append("\n  CombatSpy is off");
			}
			if (config.isTeleportSpy()) {
				sb.append("\n  TeleportSpy is on");
			} else {
				sb.append("\n  TeleportSpy is off");
			}
			return sb.toString();
		} else {
			return "Experiments disabled.";
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Must be a player to execute this command");
			return true;
		}

		Player player = (Player) sender;
		ItemStack item = player.getInventory().getItemInMainHand();
		YamlConfiguration yml = new YamlConfiguration();
		yml.set("template", item);
		plugin().log(yml.saveToString());
		sender.sendMessage(yml.saveToString());
		return true;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	private void monitorTeleportLow(PlayerTeleportEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isTeleportSpy()) return;
		StringBuilder sb = new StringBuilder("[LO] ");
		logTeleport(event, sb);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	private void monitorTeleportHigh(PlayerTeleportEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isTeleportSpy()) return;
		StringBuilder sb = new StringBuilder("[HI] ");
		logTeleport(event, sb);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void monitorTeleportTrack(PlayerPortalEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isPostTeleportSpy()) return;

		final Player player = event.getPlayer();

		new BukkitRunnable() {
			final UUID playerUUID = player.getUniqueId();
			private int runCount = 0;
			public void run() {
				runCount++;
				if (runCount > config.getPostTeleportSpyCount()) {
					this.cancel();
					return;
				}
				Player player = plugin().getServer().getPlayer(playerUUID);
				if (player != null) {
					StringBuilder sb = new StringBuilder("Tracking: ");
					sb.append(playerUUID);
					logPlayer(player, sb);
					plugin().log(sb.toString());
				} else {
					StringBuilder sb = new StringBuilder("Lost: ");
					sb.append(playerUUID);
					plugin().log(sb.toString());
					this.cancel();
				}
			}
		}.runTaskTimer(plugin(), 2l, 4l);
	}

	private void logTeleport(PlayerTeleportEvent event, StringBuilder sb) {
		sb.append(event.isCancelled() ? "C " : "A ");
		sb.append(event.getCause().name());
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();
		sb.append(String.format(" %16s", player.getName()));
		sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", from.getWorld().getName(), from.getX(), from.getY(), from.getZ()));
		sb.append(" ->");
		if (to != null) {
			sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", to.getWorld().getName(), to.getX(), to.getY(), to.getZ()));
		} else {
			sb.append(" (none)");
		}
		sb.append(" [").append(player.getUniqueId()).append(" ");
		logPlayer(player, sb);
		sb.append("]");
		plugin().log(sb.toString());
	}

	private void logPlayer(final Player player, StringBuilder sb) {
		sb.append(player.getWorld().getName());
		Location feet = player.getLocation();
		Location eyes = player.getEyeLocation();
		sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", feet.getWorld().getName(), feet.getX(), feet.getY(), feet.getZ()));
		sb.append(String.format(" %s,%5.0f,%3.0f,%5.0f", eyes.getWorld().getName(), eyes.getX(), eyes.getY(), eyes.getZ()));
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	private void monitorCombatLow(EntityDamageByEntityEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isCombatSpy()) return;
		StringBuilder sb = new StringBuilder("[LO] ");
		logCombat(event, sb);
	}

	private void logCombat(EntityDamageByEntityEvent event, StringBuilder sb) {
		sb.append(event.isCancelled() ? "C " : "A ");
		sb.append(event.getCause().name());
		sb.append(String.format(", %5.2f->%5.2f", event.getDamage(), event.getFinalDamage()));
		sb.append(String.format(", %16s v %16s", event.getDamager().getName(), event.getEntity().getName()));
		for (EntityDamageEvent.DamageModifier mod : EntityDamageEvent.DamageModifier.values()) {
			sb.append(", ").append(mod.name());
			try {
				sb.append(String.format(" %5.2f->%5.2f", event.getOriginalDamage(mod), event.getDamage(mod)));
			} catch (Exception e) {
				sb.append(" --e--/--e--");
			}
		}
		plugin().log(sb.toString());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	private void monitorCombatHigh(EntityDamageByEntityEvent event) {
		if (!config.isEnabled()) return;
		if (!config.isCombatSpy()) return;
		StringBuilder sb = new StringBuilder("[HI]: ");
		logCombat(event, sb);
	}

	public static ExperimentalConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new ExperimentalConfig(plugin, config);
	}
}

