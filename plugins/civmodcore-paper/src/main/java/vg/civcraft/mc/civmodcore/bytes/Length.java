package vg.civcraft.mc.civmodcore.bytes;

import org.jetbrains.annotations.NotNull;

public enum Length {
    u8(Byte.SIZE, Byte.BYTES, (1 << Byte.SIZE) - 1),
    u16(Short.SIZE, Short.BYTES, (1 << Short.SIZE) - 1),
    u31(Integer.SIZE, Integer.BYTES, Integer.MAX_VALUE),
    ;

    public final int bitLength;
    public final int byteLength;
    public final long max;

    Length(
        final int bitLength,
        final int byteLength,
        final long max
    ) {
        this.bitLength = bitLength;
        this.byteLength = byteLength;
        this.max = max;
    }

    public int assertValidLength(
        final @NotNull String name,
        final long length
    ) {
        if (length > this.max) {
            throw new IllegalArgumentException("'" + name + "' cannot have be >" + this.max + "! [" + length + "]");
        }
        return (int) length;
    }
}
