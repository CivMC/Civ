package com.untamedears.itemexchange.utility.nbt;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

@Deprecated
public final class NBTSerialization {
    /**
     * Dynamically retrieves a serializable's {@link NBTSerializable#fromNBT(NbtCompound) fromNBT} method.
     *
     * @param <T>   The type of the serializable.
     * @param clazz The serializable's class.
     * @return Returns a deserializer function.
     */
    @SuppressWarnings("unchecked")
    public static <T extends NBTSerializable> @NotNull NBTDeserializer<T> getDeserializer(
        final @NotNull Class<T> clazz
    ) {
        final var method = MethodUtils.getMatchingAccessibleMethod(clazz, "fromNBT", NbtCompound.class);
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
