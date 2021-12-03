package vg.civcraft.mc.civmodcore.nbt;

import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.NBTCompressedStreamTools;
import net.minecraft.nbt.NBTReadLimiter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftNBTTagConfigSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.pdc.extensions.PersistentDataContainerExtensions;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

@UtilityClass
@ExtensionMethod(PersistentDataContainerExtensions.class)
public class NBTSerialization {

	private static final CivLogger LOGGER = CivLogger.getLogger(NBTSerialization.class);

	/**
	 * Retrieves the NBT data from an item.
	 *
	 * @param item The item to retrieve the NBT form.
	 * @return Returns the item's NBT.
	 */
	public static NBTCompound fromItem(final ItemStack item) {
		if (item == null) {
			return null;
		}
		final var nmsItem = ItemUtils.getNMSItemStack(item);
		if (nmsItem == null) {
			return null;
		}
		return new NBTCompound(nmsItem.t());
	}

	/**
	 * Generates an NBT compound based on a given persistent data container.
	 *
	 * @param container The container to generate an NBT compound from.
	 * @return Returns a newly generated NBT compound by wrapping the PDC's inner-map.
	 */
	@Nonnull
	public static NBTCompound fromPDC(@Nonnull final PersistentDataContainer container) {
		return new NBTCompound(container.getRaw());
	}

	/**
	 * Processes an item's NBT before setting again.
	 *
	 * @param item The item to process.
	 * @param processor The processor.
	 * @return Returns the given item with the processed NBT, or null if it could not be successfully processed.
	 */
	public static ItemStack processItem(final ItemStack item, final Consumer<NBTCompound> processor) {
		Preconditions.checkArgument(ItemUtils.isValidItem(item));
		Preconditions.checkArgument(processor != null);
		final var nmsItem = ItemUtils.getNMSItemStack(item);
		if (nmsItem == null) {
			return null;
		}
		final var nbt = new NBTCompound(nmsItem.t());
		try {
			processor.accept(nbt);
		}
		catch (final Throwable exception) {
			LOGGER.log(Level.WARNING, "Could not process item NBT!", exception);
			return null;
		}
		return nmsItem.getBukkitStack();
	}

	public static NBTTagCompound fromMap(final Map<String, Object> data) {
		return (NBTTagCompound) CraftNBTTagConfigSerializer.deserialize(data);
	}

	public static NBTTagList fromList(final List<Object> data) {
		return (NBTTagList) CraftNBTTagConfigSerializer.deserialize(data);
	}

	/**
	 * Attempts to serialize an NBTCompound into a data array.
	 *
	 * @param nbt The NBTCompound to serialize.
	 * @return Returns a data array representing the given NBTCompound serialized, or otherwise null.
	 */
	public static byte[] toBytes(final NBTTagCompound nbt) {
		if (nbt == null) {
			return null;
		}
		final ByteArrayDataOutput output = ByteStreams.newDataOutput();
		try {
			NBTCompressedStreamTools.a(nbt, output);
		}
		catch (final IOException exception) {
			LOGGER.log(Level.WARNING, "Could not serialise NBT to bytes!", exception);
			return null;
		}
		return output.toByteArray();
	}

	/**
	 * Attempts to deserialize NBT data into an NBTCompound.
	 *
	 * @param bytes The NBT data as a byte array.
	 * @return Returns an NBTCompound if the deserialization was successful, or otherwise null.
	 */
	public static NBTTagCompound fromBytes(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		final ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
		try {
			return NBTCompressedStreamTools.a(input, NBTReadLimiter.a);
		}
		catch (final IOException exception) {
			LOGGER.log(Level.WARNING, "Could not deserialise NBT from bytes!", exception);
			return null;
		}
	}

	/**
	 * Dynamically retrieves a serializable's {@link NBTSerializable#fromNBT(NBTCompound) fromNBT} method.
	 *
	 * @param <T> The type of the serializable.
	 * @param clazz The serializable's class.
	 * @return Returns a deserializer function.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends NBTSerializable> NBTDeserializer<T> getDeserializer(final Class<T> clazz) {
		final var method = MethodUtils.getMatchingAccessibleMethod(clazz, "fromNBT", NBTCompound.class);
		if (!Objects.equals(clazz, method.getReturnType())) {
			throw new IllegalArgumentException("That class hasn't implemented its own fromNBT method.. please fix");
		}
		return (nbt) -> {
			try {
				return (T) method.invoke(null, nbt);
			}
			catch (final IllegalAccessException | InvocationTargetException exception) {
				throw new NBTSerializationException(exception);
			}
		};
	}

}
