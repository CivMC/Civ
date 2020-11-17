package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.autoload.DataParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;

public class ShipOutOfLuck extends BasicHack {

	@AutoLoad(processor = DataParser.MATERIAL)
	private List<Material> boatBreakers = new ArrayList<>();

	@AutoLoad
	private boolean incongruousBoats;

	public ShipOutOfLuck(SimpleAdminHacks plugin, BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public String status() {
		final StringBuilder builder = new StringBuilder(getClass().getSimpleName())
				.append(" is ")
				.append(isEnabled() ? "enabled" : "disabled")
				.append(".")
				.append("\n");
		if (this.boatBreakers.isEmpty()) {
			builder.append("No boat breakers.");
		}
		else {
			builder.append(" Boat breakers:\n");
			builder.append(this.boatBreakers.stream()
					.map(material -> "  • " + material.name())
					.collect(Collectors.joining("\n")));
		}
		return builder.toString();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventBoatPlacements(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		final Block placedOn = Objects.requireNonNull(event.getClickedBlock());
		final ItemStack placed = event.getItem();
		if (!this.boatBreakers.contains(placedOn.getType())
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
	public void preventBoatUsage(final VehicleMoveEvent event) {
		final Vehicle vehicle = event.getVehicle();
		if (vehicle.getType() != EntityType.BOAT) {
			return;
		}
		final List<Entity> passengers = vehicle.getPassengers();
		if (passengers.isEmpty()) {
			return;
		}
		final Location destination = event.getTo();
		if (vehicle.isOnGround()) {
			// If the boat is on top of a block, then we need to look below the boat
			destination.add(0, -1, 0);
		}
		if (destination.getY() < 0 || destination.getY() >= destination.getWorld().getMaxHeight()) {
			return;
		}
		final Block currentBlock = destination.getBlock();
		final Material currentMaterial = currentBlock.getType();
		if (this.boatBreakers.contains(currentMaterial)) {
			vehicle.eject();
			plugin().debug("Ejected [" + passengers.stream().map(CommandSender::getName)
					.collect(Collectors.joining(", ")) + "] from boat at [" + destination + "] because " +
					"they sailed over [" + currentMaterial.name() + "]");
		}
		else if (!this.incongruousBoats
				&& vehicle.isOnGround()
				&& MaterialAPI.isWithoutSubstance(currentMaterial)) {
			vehicle.eject();
			plugin().debug("Ejected [" + passengers.stream().map(CommandSender::getName)
					.collect(Collectors.joining(", ")) + "] from boat at [" + destination + "] because " +
					"the boat was grounded while sailing over [" + currentMaterial.name() + "]");
		}
		//return;
	}

	public static BasicHackConfig generate(SimpleAdminHacks plugin, ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
