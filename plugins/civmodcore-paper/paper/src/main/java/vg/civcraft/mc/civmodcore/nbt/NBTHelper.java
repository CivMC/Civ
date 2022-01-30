package vg.civcraft.mc.civmodcore.nbt;

import java.util.UUID;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.extensions.NBTTagCompoundExtensions;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.UuidUtils;

@UtilityClass
@ExtensionMethod(NBTTagCompoundExtensions.class)
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
				UuidUtils.isNullOrIdentity(worldUUID) ? null : Bukkit.getWorld(worldUUID),
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
		nbt.setUUID(LOCATION_WORLD_KEY, location.isWorldLoaded() ? location.getWorld().getUID() : UuidUtils.IDENTITY);
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
		return net.minecraft.world.item.ItemStack.of(nbt.getRAW()).getBukkitStack();
	}

	public static NBTCompound itemStackToNBT(final ItemStack item) {
		if (item == null) {
			return null;
		}
		final var nbt = new NBTCompound();
		ItemUtils.getNMSItemStack(item).save(nbt.getRAW());
		return nbt;
	}

}
