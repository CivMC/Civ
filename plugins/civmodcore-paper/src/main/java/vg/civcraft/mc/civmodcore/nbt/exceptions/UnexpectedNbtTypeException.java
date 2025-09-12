package vg.civcraft.mc.civmodcore.nbt.exceptions;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.NotNull;

/**
 * This is for when you expected, for example, a {@link net.minecraft.nbt.StringTag} but got a {@link net.minecraft.nbt.ByteTag}
 * instead.
 *
 * @apiNote This will likely only be relevant when interacting with Mojang NBT since most other things have a dedicated
 *          {@code .getString()} method, for example.
 */
public final class UnexpectedNbtTypeException extends NbtException {
    public UnexpectedNbtTypeException(
        final @NotNull Class<? extends Tag> expected,
        final @NotNull Object found
    ) {
        super("Expected type [" + expected.getName() + "], but found [" + found.getClass().getName() + "] instead!");
    }
}
