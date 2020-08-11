package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.SimpleHack;
import com.programmerdan.minecraft.simpleadminhacks.configs.ShipOutOfLuckConfig;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;

public class ShipOutOfLuck extends SimpleHack<ShipOutOfLuckConfig> implements Listener {

	public ShipOutOfLuck(SimpleAdminHacks plugin, ShipOutOfLuckConfig config) {
		super(plugin, config);
	}

	@Override
	public String status() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(" is ").append(isEnabled() ? "enabled" : "disabled").append(".").append("\n");
		Set<Material> scuttleList = this.config.getBoatBreakers();
		if (scuttleList.isEmpty()) {
			builder.append("No scuttle blocks.");
		}
		else {
			builder.append(" Scuttle blocks:\n");
			builder.append(scuttleList.stream()
					.map(effect -> "  • " + effect.name())
					.collect(Collectors.joining("\n")));
		}
		return builder.toString();
	}

	// ------------------------------------------------------------
	// Listeners
	// ------------------------------------------------------------

	@Override
	public void registerListeners() {
		this.plugin().registerListener(this);
	}

	@Override
	public void unregisterListeners() {
		HandlerList.unregisterAll(this);
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventBoatPlacements(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		Block placedOn = Objects.requireNonNull(event.getClickedBlock());
		ItemStack placed = event.getItem();
		if (!this.config.isBoatBreaker(placedOn.getType())
				|| !ItemAPI.isValidItem(placed)
				|| !Tag.ITEMS_BOATS.isTagged(placed.getType())) {
			return;
		}
		event.setCancelled(true);
		plugin().debug("Prevented boat placement on [" + placedOn.getType().name() + "] by [" +
				event.getPlayer().getName() + "] at [" + placedOn.getLocation() + "]");
		//return;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventBoatUsage(VehicleMoveEvent event) {
		Vehicle vehicle = event.getVehicle();
		if (vehicle.getType() != EntityType.BOAT) {
			return;
		}
		List<Entity> passengers = vehicle.getPassengers();
		if (passengers.isEmpty()) {
			return;
		}
		Location destination = event.getTo();
		if (event.getVehicle().isOnGround()) {
			// If the boat is on top of a block, then we need to look below the boat
			destination.add(0, -1, 0);
		}
		if (destination.getY() < 0 || destination.getY() >= destination.getWorld().getMaxHeight()) {
			return;
		}
		Block currentBlock = destination.getBlock();
		if (!this.config.isBoatBreaker(currentBlock.getType())) {
			return;
		}
		event.getVehicle().eject();
		plugin().debug("Ejected [" + passengers.stream().map(CommandSender::getName)
				.collect(Collectors.joining(", ")) + "] from boat at [" + destination + "] because they " +
				"sailed over [" + currentBlock.getType().name() + "]");
		//return;
	}

	// ------------------------------------------------------------
	// Commands
	// ------------------------------------------------------------

	@Override
	public void registerCommands() { }

	@Override
	public void unregisterCommands() { }

	// ------------------------------------------------------------
	// Setup
	// ------------------------------------------------------------

	@Override
	public void dataBootstrap() { }

	@Override
	public void dataCleanup() { }

	public static ShipOutOfLuckConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new ShipOutOfLuckConfig(plugin, config);
	}

}
