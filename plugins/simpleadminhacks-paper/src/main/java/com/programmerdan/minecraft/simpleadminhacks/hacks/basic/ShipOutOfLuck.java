package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.AutoLoad;
import com.programmerdan.minecraft.simpleadminhacks.framework.autoload.DataParser;
import com.programmerdan.minecraft.simpleadminhacks.framework.utilities.BetterToString;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPosition;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
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
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.world.ChunkLoadedFilter;

public final class ShipOutOfLuck extends BasicHack {

	@AutoLoad(processor = DataParser.MATERIAL)
	private List<Material> boatBreakers;

	private int ignoredBoatCounter = 0;

	public ShipOutOfLuck(final SimpleAdminHacks plugin, final BasicHackConfig config) {
		super(plugin, config);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		// Just in case it's auto loaded to null.
		if (this.boatBreakers == null) {
			this.boatBreakers = new ArrayList<>();
		}
	}

	@Override
	public void onDisable() {
		this.boatBreakers.clear();
		super.onDisable();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void preventBoatPlacements(final PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}
		final Block placedOn = Objects.requireNonNull(event.getClickedBlock());
		final ItemStack placed = event.getItem();
		if (!this.boatBreakers.contains(placedOn.getType())
				|| !ItemUtils.isValidItem(placed)
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
		if (++this.ignoredBoatCounter < 40) {
			return;
		}
		this.ignoredBoatCounter = 0;
		final World world = vehicle.getWorld();
		final List<Material> illegalBlocks = getCollidingBlocks(vehicle.getBoundingBox())
				.filter(ChunkLoadedFilter.blockPosition(world))
				.map(loc -> world.getBlockAt(loc.getX(), loc.getY(), loc.getZ()).getType())
				.filter(this.boatBreakers::contains)
				.distinct()
				.collect(Collectors.toCollection(ArrayList::new));
		if (CollectionUtils.isEmpty(illegalBlocks)) {
			return;
		}
		vehicle.eject();
		plugin().debug("Ejected [" + passengers.stream().map(CommandSender::getName)
				.collect(Collectors.joining(", ")) + "] from boat at [" + BetterToString.location(event.getTo())
				+ "] because they sailed over [" + illegalBlocks.stream().map(Material::name)
				.collect(Collectors.joining(", ")) + "]");
		//return;
	}

	private static Stream<BlockPosition> getCollidingBlocks(final BoundingBox bounds) {
		// This transform the bounds of the boat to be a flat rectangle roughly a carpet's thickness smaller on each
		// side and placed the same distance below the boat, which should be enough to reliably detect what blocks
		// are carrying the boat without false alarming with blocks beside the boat.
		final int minX = (int) Math.floor(bounds.getMinX() + 0.06);
		final int maxX = (int) Math.floor(bounds.getMaxX() - 0.06);
		final int valY = (int) Math.floor(bounds.getMinY() - 0.06);
		final int minZ = (int) Math.floor(bounds.getMinZ() + 0.06);
		final int maxZ = (int) Math.floor(bounds.getMaxZ() - 0.06);
		return BlockPosition.a(minX, valY, minZ, maxX, valY, maxZ);
	}

}
