package com.programmerdan.minecraft.simpleadminhacks.hacks;

import com.programmerdan.minecraft.simpleadminhacks.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.autoload.DataParser;
import com.programmerdan.minecraft.simpleadminhacks.util.BetterToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import net.minecraft.server.v1_16_R1.BlockPosition;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
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
import org.bukkit.util.BoundingBox;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.util.Iteration;

public final class ShipOutOfLuck extends BasicHack {

	@AutoLoad(processor = DataParser.MATERIAL)
	private List<Material> boatBreakers;

	public ShipOutOfLuck(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
		// Just in case it's auto loaded to null.
		if (this.boatBreakers == null) {
			this.boatBreakers = new ArrayList<>();
		}
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
				event.getPlayer().getName() + "] at [" + BetterToString.location(placedOn.getLocation()) + "]");
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
		if (ThreadLocalRandom.current().nextDouble() < 0.2d) { // Only check 20% of the time
			return;
		}
		final World world = vehicle.getWorld();
		final BoundingBox defaultBounds = vehicle.getBoundingBox();
		final List<Material> illegalBlocks = BlockPosition.a(
				(int) defaultBounds.getMinX(), (int) (defaultBounds.getMinY() - 0.06d), (int) defaultBounds.getMinZ(),
				(int) defaultBounds.getMaxX(), (int) (defaultBounds.getMinY() - 0.06d), (int) defaultBounds.getMaxZ())
				.filter(loc -> world.isChunkLoaded(loc.getX() >> 4, loc.getZ() >> 4))
				.map(loc -> world.getBlockAt(loc.getX(), loc.getY(), loc.getZ()).getType())
				.filter(this.boatBreakers::contains)
				.distinct()
				.collect(Collectors.toCollection(ArrayList::new));
		if (Iteration.isNullOrEmpty(illegalBlocks)) {
			return;
		}
		vehicle.eject();
		plugin().debug("Ejected [" + passengers.stream().map(CommandSender::getName)
				.collect(Collectors.joining(", ")) + "] from boat at [" + BetterToString.location(event.getTo())
				+ "] because they sailed over [" + illegalBlocks.stream().map(Material::name)
				.collect(Collectors.joining(", ")) + "]");
		//return;
	}

	public static BasicHackConfig generate(final SimpleAdminHacks plugin, final ConfigurationSection config) {
		return new BasicHackConfig(plugin, config);
	}

}
