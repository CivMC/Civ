package vg.civcraft.mc.civmodcore.nbt;

import com.google.common.annotations.Beta;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.craftbukkit.util.CraftNBTTagConfigSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.CivLogger;

public final class NBTSerialization {

    private static final CivLogger LOGGER = CivLogger.getLogger(NBTSerialization.class);

    @Beta
	public static CompoundTag fromMap(final Map<String, Object> data) {
		return (CompoundTag) CraftNBTTagConfigSerializer.deserialize(data);
	}

    @Beta
    public static ListTag fromList(final List<Object> data) {
        return (ListTag) CraftNBTTagConfigSerializer.deserialize(data);
    }

    /**
     * Attempts to serialize an NBTCompound into a data array.
     *
     * @param nbt The NBTCompound to serialize.
     * @return Returns a data array representing the given NBTCompound serialized, or otherwise null.
     */
    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    public static byte[] toBytes(final CompoundTag nbt) {
        if (nbt == null) {
            return null;
        }
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        try {
            NbtIo.write(nbt, output);
        } catch (final IOException exception) {
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
    @SuppressWarnings("UnstableApiUsage")
    @Nullable
    public static CompoundTag fromBytes(final byte[] bytes) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        final ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        try {
            return NbtIo.read(input, NbtAccounter.unlimitedHeap());
        } catch (final IOException exception) {
            LOGGER.log(Level.WARNING, "Could not deserialise NBT from bytes!", exception);
            return null;
        }
    }

    /**
     * Dynamically retrieves a serializable's {@link NBTSerializable#fromNBT(NBTCompound) fromNBT} method.
     *
     * @param <T>   The type of the serializable.
     * @param clazz The serializable's class.
     * @return Returns a deserializer function.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    public static <T extends NBTSerializable> NBTDeserializer<T> getDeserializer(@NotNull final Class<T> clazz) {
        final var method = MethodUtils.getMatchingAccessibleMethod(clazz, "fromNBT", NBTCompound.class);
        if (!Objects.equals(clazz, method.getReturnType())) {
            throw new IllegalArgumentException("That class hasn't implemented its own fromNBT method.. please fix");
        }
        return (nbt) -> {
            try {
                return (T) method.invoke(null, nbt);
            } catch (final IllegalAccessException | InvocationTargetException exception) {
                throw new NBTSerializationException(exception);
            }
        };
    }

}
