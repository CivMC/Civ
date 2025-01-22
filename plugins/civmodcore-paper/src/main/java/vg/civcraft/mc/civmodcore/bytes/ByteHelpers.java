package vg.civcraft.mc.civmodcore.bytes;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;

public final class ByteHelpers {
    public static @NotNull ByteArrayDataOutput newPacketWriter(
        final int initialCapacity
    ) {
        return ByteStreams.newDataOutput(initialCapacity);
    }

    public static <T> void writeArray(
        final @NotNull ByteArrayDataOutput out,
        final T @NotNull [] array,
        final @NotNull Length lengthPrefix,
        final @NotNull BiConsumer<@NotNull ByteArrayDataOutput, T> elementWriter
    ) {
        final int length = lengthPrefix.assertValidLength("array.length", array.length);
        switch (lengthPrefix) {
            case u8 -> out.writeByte(length);
            case u16 -> out.writeShort(length);
            case u31 -> out.writeInt(length);
        }
        for (final T element : array) {
            elementWriter.accept(out, element);
        }
    }

    public static <T> void writeCollection(
        final @NotNull ByteArrayDataOutput out,
        @NotNull Collection<T> collection,
        final @NotNull Length lengthPrefix,
        final @NotNull BiConsumer<@NotNull ByteArrayDataOutput, T> elementWriter
    ) {
        collection = new ArrayList<>(collection); // Just in case
        final int length = lengthPrefix.assertValidLength("collection.size()", collection.size());
        switch (lengthPrefix) {
            case u8 -> out.writeByte(length);
            case u16 -> out.writeShort(length);
            case u31 -> out.writeInt(length);
        }
        for (final T element : collection) {
            elementWriter.accept(out, element);
        }
    }
}
