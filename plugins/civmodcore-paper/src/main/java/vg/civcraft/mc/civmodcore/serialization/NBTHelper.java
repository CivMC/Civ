package vg.civcraft.mc.civmodcore.serialization;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public final class NBTHelper {

	// ------------------------------------------------------------
	// Location
	// ------------------------------------------------------------

	private static final String LOCATION_WORLD_KEY = "world";
	private static final String LOCATION_X_KEY = "x";
	private static final String LOCATION_Y_KEY = "y";
	private static final String LOCATION_Z_KEY = "z";
	private static final String LOCATION_YAW_KEY = "yaw";
	private static final String LOCATION_PITCH_KEY = "pitch";

	public static Location locationFromNBT(final NBTCompound nbt) {
		if (nbt == null) {
			return null;
		}
		final UUID worldUUID = nbt.getUUID(LOCATION_WORLD_KEY);
		return new Location(
				worldUUID == null ? null : Bukkit.getWorld(worldUUID),
				nbt.getDouble(LOCATION_X_KEY),
				nbt.getDouble(LOCATION_Y_KEY),
				nbt.getDouble(LOCATION_Z_KEY),
				nbt.getFloat(LOCATION_YAW_KEY),
				nbt.getFloat(LOCATION_PITCH_KEY));
	}

	public static NBTCompound locationToNBT(final Location location) {
		if (location == null) {
			return null;
		}
		final var nbt = new NBTCompound();
		nbt.setUUID(LOCATION_WORLD_KEY, location.isWorldLoaded() ? location.getWorld().getUID() : null);
		nbt.setDouble(LOCATION_X_KEY, location.getX());
		nbt.setDouble(LOCATION_Y_KEY, location.getY());
		nbt.setDouble(LOCATION_Z_KEY, location.getZ());
		if (location.getYaw() != 0) {
			nbt.setFloat(LOCATION_YAW_KEY, location.getYaw());
		}
		if (location.getPitch() != 0) {
			nbt.setFloat(LOCATION_PITCH_KEY, location.getPitch());
		}
		return nbt;
	}

	// ------------------------------------------------------------
	// ItemStack
	// ------------------------------------------------------------

	public static ItemStack itemStackFromNBT(final NBTCompound nbt) {
		if (nbt == null) {
			return null;
		}
		return net.minecraft.server.v1_16_R3.ItemStack.fromCompound(nbt.getRAW()).getBukkitStack();
	}

	public static NBTCompound itemStackToNBT(final ItemStack item) {
		if (item == null) {
			return null;
		}
		final var nbt = new NBTCompound();
		CraftItemStack.asNMSCopy(item).save(nbt.getRAW());
		return nbt;
	}

}
