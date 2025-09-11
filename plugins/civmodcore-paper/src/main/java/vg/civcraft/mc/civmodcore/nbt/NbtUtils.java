package vg.civcraft.mc.civmodcore.nbt;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NbtUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(NbtUtils.class);

    public static byte @Nullable [] toBytes(
        final CompoundTag nbt
    ) {
        if (nbt == null) {
            return null;
        }
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        try {
            NbtIo.write(nbt, output);
        } catch (final IOException exception) {
            LOGGER.warn("Could not serialise NBT to bytes!", exception);
            return null;
        }
        return output.toByteArray();
    }

    public static @Nullable CompoundTag fromBytes(
        final byte[] bytes
    ) {
        if (ArrayUtils.isEmpty(bytes)) {
            return null;
        }
        final ByteArrayDataInput input = ByteStreams.newDataInput(bytes);
        try {
            return NbtIo.read(input, NbtAccounter.unlimitedHeap());
        } catch (final IOException exception) {
            LOGGER.warn("Could not deserialise NBT from bytes!", exception);
            return null;
        }
    }
}
