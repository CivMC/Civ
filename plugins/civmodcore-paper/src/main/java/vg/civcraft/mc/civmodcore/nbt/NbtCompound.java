package vg.civcraft.mc.civmodcore.nbt;

import com.mojang.serialization.Dynamic;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import vg.civcraft.mc.civmodcore.nbt.exceptions.InvalidNbtValue;
import vg.civcraft.mc.civmodcore.nbt.exceptions.UnexpectedNbtTypeException;

public record NbtCompound(
    @NotNull CompoundTag internal
) {
    public NbtCompound {
        Objects.requireNonNull(internal);
    }

    public NbtCompound() {
        this(new CompoundTag());
    }

    public int size() {
        return internal().size();
    }

    public boolean isEmpty() {
        return internal().isEmpty();
    }

    public @UnmodifiableView @NotNull Set<@NotNull String> keys() {
        return Collections.unmodifiableSet(internal().keySet());
    }

    public void remove(
        final @NotNull String key
    ) {
        internal().remove(Objects.requireNonNull(key));
    }

    public void clear() {
        internal().keySet().clear();
    }

    @ApiStatus.Internal
    public @Nullable Tag get(
        final @NotNull String key
    ) {
        return internal().get(Objects.requireNonNull(key));
    }

    @Contract("_, !null -> !null")
    public @Nullable Boolean getBoolean(
        final @NotNull String key,
        final Boolean fallbackIfNull
    ) {
        return switch (get(key)) {
            case final ByteTag match -> switch (match.byteValue()) {
                case 0 -> false;
                case 1 -> true;
                default -> throw new InvalidNbtValue();
            };
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ByteTag.class, unknown);
        };
    }

    public void setBoolean(
        final @NotNull String key,
        final boolean value
    ) {
        internal().putBoolean(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Byte getByte(
        final @NotNull String key,
        final Byte fallbackIfNull
    ) {
        return switch (get(key)) {
            case final ByteTag match -> match.byteValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ByteTag.class, unknown);
        };
    }

    public void setByte(
        final @NotNull String key,
        final byte value
    ) {
        internal().putByte(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Short getShort(
        final @NotNull String key,
        final Short fallbackIfNull
    ) {
        return switch (get(key)) {
            case final ShortTag match -> match.shortValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ShortTag.class, unknown);
        };
    }

    public void setShort(
        final @NotNull String key,
        final short value
    ) {
        internal().putShort(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Integer getInt(
        final @NotNull String key,
        final Integer fallbackIfNull
    ) {
        return switch (get(key)) {
            case final IntTag match -> match.intValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(IntTag.class, unknown);
        };
    }

    public void setInt(
        final @NotNull String key,
        final int value
    ) {
        internal().putInt(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Long getLong(
        final @NotNull String key,
        final Long fallbackIfNull
    ) {
        return switch (get(key)) {
            case final LongTag match -> match.longValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(LongTag.class, unknown);
        };
    }

    public void setLong(
        final @NotNull String key,
        final long value
    ) {
        internal().putLong(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Float getFloat(
        final @NotNull String key,
        final Float fallbackIfNull
    ) {
        return switch (get(key)) {
            case final FloatTag match -> match.floatValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(FloatTag.class, unknown);
        };
    }

    public void setFloat(
        final @NotNull String key,
        final float value
    ) {
        internal().putFloat(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable Double getDouble(
        final @NotNull String key,
        final Double fallbackIfNull
    ) {
        return switch (get(key)) {
            case final DoubleTag match -> match.doubleValue();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(DoubleTag.class, unknown);
        };
    }

    public void setDouble(
        final @NotNull String key,
        final double value
    ) {
        internal().putDouble(Objects.requireNonNull(key), value);
    }

    @Contract("_, !null -> !null")
    public @Nullable UUID getUuid(
        final @NotNull String key,
        final UUID fallbackIfNull
    ) {
        return switch (get(key)) {
            case final IntArrayTag match -> UUIDUtil.readUUID(new Dynamic<>(NbtOps.INSTANCE, match));
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(IntArrayTag.class, unknown);
        };
    }

    public void setUuid(
        final @NotNull String key,
        final UUID value
    ) {
        if (value == null) {
            remove(key);
            return;
        }
        setIntArray(key, UUIDUtil.uuidToIntArray(value));
    }

    @Contract("_, !null -> !null")
    public @Nullable String getString(
        final @NotNull String key,
        final String fallbackIfNull
    ) {
        return switch (get(key)) {
            case final StringTag match -> match.value();
            case null -> fallbackIfNull;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(StringTag.class, unknown);
        };
    }

    public void setString(
        final @NotNull String key,
        final String value
    ) {
        Objects.requireNonNull(key);
        if (value == null) {
            remove(key);
        } else {
            internal().putString(key, value);
        }
    }

    @Contract("_, true -> !null")
    public @Nullable NbtCompound getCompound(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final CompoundTag match -> new NbtCompound(match);
            case null -> createEmptyIfNull ? new NbtCompound() : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(CompoundTag.class, unknown);
        };
    }

    public void setCompound(
        final @NotNull String key,
        final NbtCompound value
    ) {
        Objects.requireNonNull(key);
        if (value == null) {
            remove(key);
        } else {
            internal().put(key, value.internal());
        }
    }

    // ------------------------------------------------------------
    // Array Functions
    // ------------------------------------------------------------

    @Contract("_, true -> !null")
    public byte @Nullable [] getByteArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ByteArrayTag match -> match.getAsByteArray();
            case null -> createEmptyIfNull ? new byte[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ByteArrayTag.class, unknown);
        };
    }

    public void setByteArray(
        final @NotNull String key,
        final byte[] bytes
    ) {
        if (bytes == null) {
            remove(key);
            return;
        }
        internal().putByteArray(Objects.requireNonNull(key), bytes);
    }

    @Contract("_, true -> !null")
    public short @Nullable [] getShortArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new short[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final ShortTag element -> element.shortValue();
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new InvalidNbtValue(new UnexpectedNbtTypeException(ShortTag.class, unknown));
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new short[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    public void setShortArray(
        final @NotNull String key,
        final short[] shorts
    ) {
        if (shorts == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final short value : shorts) {
            list.add(ShortTag.valueOf(value));
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    @Contract("_, true -> !null")
    public int @Nullable [] getIntArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final IntArrayTag match -> match.getAsIntArray();
            case null -> createEmptyIfNull ? new int[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(IntArrayTag.class, unknown);
        };
    }

    public void setIntArray(
        final @NotNull String key,
        final int[] ints
    ) {
        if (ints == null) {
            remove(key);
            return;
        }
        internal().putIntArray(Objects.requireNonNull(key), ints);
    }

    @Contract("_, true -> !null")
    public long @Nullable [] getLongArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final LongArrayTag match -> match.getAsLongArray();
            case null -> createEmptyIfNull ? new long[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(LongArrayTag.class, unknown);
        };
    }

    public void setLongArray(
        final @NotNull String key,
        final long[] longs
    ) {
        if (longs == null) {
            remove(key);
            return;
        }
        internal().putLongArray(Objects.requireNonNull(key), longs);
    }

    @Contract("_, true -> !null")
    public float @Nullable [] getFloatArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new float[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final FloatTag element -> element.floatValue();
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new InvalidNbtValue(new UnexpectedNbtTypeException(FloatTag.class, unknown));
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new float[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    public void setFloatArray(
        final @NotNull String key,
        final float[] floats
    ) {
        if (floats == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final float value : floats) {
            list.add(FloatTag.valueOf(value));
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    @Contract("_, true -> !null")
    public double @Nullable [] getDoubleArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new double[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final DoubleTag element -> element.doubleValue();
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new InvalidNbtValue(new UnexpectedNbtTypeException(DoubleTag.class, unknown));
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new double[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    public void setDoubleArray(
        final @NotNull String key,
        final double[] doubles
    ) {
        if (doubles == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final double value : doubles) {
            list.add(DoubleTag.valueOf(value));
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    @Contract("_, true -> !null")
    public @NotNull UUID @Nullable [] getUuidArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new UUID[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final IntArrayTag element -> UUIDUtil.readUUID(new Dynamic<>(NbtOps.INSTANCE, element));
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new UnexpectedNbtTypeException(IntArrayTag.class, unknown);
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new UUID[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    /**
     * Sets an array of UUIDs to a key.
     *
     * @param key   The key to set to values to.
     * @param uuids The values to set to the key.
     */
    public void setUuidArray(
        final @NotNull String key,
        final UUID[] uuids
    ) {
        if (uuids == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final UUID value : uuids) {
            list.add(new IntArrayTag(UUIDUtil.uuidToIntArray(value)));
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    @Contract("_, true -> !null")
    public @NotNull String @Nullable [] getStringArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new String[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final StringTag element -> element.value();
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new InvalidNbtValue(new UnexpectedNbtTypeException(StringTag.class, unknown));
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new String[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    public void setStringArray(
        final @NotNull String key,
        final @NotNull String @Nullable [] strings
    ) {
        if (strings == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final String value : strings) {
            list.add(StringTag.valueOf(value));
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    @Contract("_, true -> !null")
    public @NotNull NbtCompound @Nullable [] getCompoundArray(
        final @NotNull String key,
        final boolean createEmptyIfNull
    ) {
        return switch (get(key)) {
            case final ListTag match -> {
                final var array = new NbtCompound[match.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = switch (match.get(i)) {
                        case final CompoundTag element -> new NbtCompound(element);
                        case null -> throw new InvalidNbtValue(new NullPointerException());
                        case final Tag unknown -> throw new InvalidNbtValue(new UnexpectedNbtTypeException(CompoundTag.class, unknown));
                    };
                }
                yield array;
            }
            case null -> createEmptyIfNull ? new NbtCompound[0] : null;
            case final Tag unknown -> throw new UnexpectedNbtTypeException(ListTag.class, unknown);
        };
    }

    public void setCompoundArray(
        final @NotNull String key,
        final @NotNull NbtCompound @Nullable [] compounds
    ) {
        if (compounds == null) {
            remove(key);
            return;
        }
        final var list = new ListTag();
        for (final NbtCompound value : compounds) {
            list.add(value.internal());
        }
        internal().put(Objects.requireNonNull(key), list);
    }

    // ------------------------------------------------------------
    // Convenience functions
    // ------------------------------------------------------------

    public @NotNull NbtCompound ensureCompound(
        final @NotNull String key
    ) {
        Objects.requireNonNull(key);
        if (internal().get(key) instanceof final CompoundTag tag) {
            return new NbtCompound(tag);
        }
        final var created = new CompoundTag();
        internal().put(key, created);
        return new NbtCompound(created);
    }

    @Contract("_, _, !null -> !null")
    public <T extends Enum<T>> @Nullable T getEnum(
        final @NotNull String key,
        final @NotNull Class<T> enumClass,
        final T fallbackIfNull
    ) {
        return EnumUtils.getEnum(enumClass, getString(key, null), fallbackIfNull);
    }

    public void setEnum(
        final @NotNull String key,
        final Enum<?> value
    ) {
        setString(key, value == null ? null : value.name());
    }
}

