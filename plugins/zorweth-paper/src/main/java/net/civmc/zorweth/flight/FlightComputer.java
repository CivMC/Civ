package net.civmc.zorweth.flight;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

// Represents shared logic
public class FlightComputer {

    public static final BlockVector3 RELATIVE_POSITION = BlockVector3.at(5, 17, 7);
    public static final NamespacedKey ROCKET_COMPUTER_KEY = new NamespacedKey("zorweth", "rocket_computer");
    public static final NamespacedKey ROCKET_FUEL_KEY = new NamespacedKey("zorweth", "rocket_fuel");
    public static final NamespacedKey ROCKET_DESTINATION_X_KEY = new NamespacedKey("zorweth", "rocket_destination_x");
    public static final NamespacedKey ROCKET_DESTINATION_Z_KEY = new NamespacedKey("zorweth", "rocket_destination_z");

    public static Block getRocketOrigin(final Block computer) {
        return computer.getRelative(
            -RELATIVE_POSITION.getX(),
            -RELATIVE_POSITION.getY(),
            -RELATIVE_POSITION.getZ()
        );
    }

    public static boolean isFlightComputerPosition(final BlockVector3 relative) {
        return RELATIVE_POSITION.equals(relative);
    }

    public static boolean isFuel(final ItemStack item) {
        return item != null && item.getType() == Material.CHARCOAL;
    }

    public static boolean isSittingWithGSit(final Player player) {
        if (!player.isInsideVehicle()) {
            return false;
        }
        Material on = player.getLocation().getBlock().getRelative(BlockFace.UP).getType();
        if (!(on == Material.POLISHED_ANDESITE_STAIRS || on == Material.POLISHED_ANDESITE_SLAB)) {
            return false;
        }
        final Entity vehicle = player.getVehicle();
        return vehicle != null && vehicle.getScoreboardTags().contains("GSit_sit");
    }

    public static double getFuelKg(final Block computer) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        return dispenser.getPersistentDataContainer().getOrDefault(FlightComputer.ROCKET_FUEL_KEY, PersistentDataType.DOUBLE, 0D);
    }

    public static void setFuelKg(final Block computer, final double fuelKg) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        if (fuelKg <= 0) {
            dispenser.getPersistentDataContainer().remove(FlightComputer.ROCKET_FUEL_KEY);
        } else {
            dispenser.getPersistentDataContainer().set(ROCKET_FUEL_KEY, PersistentDataType.DOUBLE, fuelKg);
        }
        dispenser.update(true, false);
    }

    public static FlightComputerGui.Coordinates getDestination(final Block computer) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        final Integer x = dispenser.getPersistentDataContainer().get(ROCKET_DESTINATION_X_KEY, PersistentDataType.INTEGER);
        final Integer z = dispenser.getPersistentDataContainer().get(ROCKET_DESTINATION_Z_KEY, PersistentDataType.INTEGER);
        if (x == null || z == null) {
            return null;
        }
        return new FlightComputerGui.Coordinates(x, z);
    }

    public static void setDestination(final Block computer, final FlightComputerGui.Coordinates coordinates) {
        final Dispenser dispenser = (Dispenser) computer.getState(false);
        dispenser.getPersistentDataContainer().set(ROCKET_DESTINATION_X_KEY, PersistentDataType.INTEGER, coordinates.x());
        dispenser.getPersistentDataContainer().set(ROCKET_DESTINATION_Z_KEY, PersistentDataType.INTEGER, coordinates.z());
        dispenser.update(true, false);
    }
}
