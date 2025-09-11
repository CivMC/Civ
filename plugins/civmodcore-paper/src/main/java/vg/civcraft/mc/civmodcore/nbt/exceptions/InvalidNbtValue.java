package vg.civcraft.mc.civmodcore.nbt.exceptions;

import org.jetbrains.annotations.NotNull;

/**
 * This is when an NBT value is the correct type but has an invalid or unexpected value. For example, booleans are
 * stored as bytes, so this is thrown if that byte represents any value other than 0 or 1.
 */
public final class InvalidNbtValue extends NbtException {
    public InvalidNbtValue() {
        super();
    }

    /**
     * @apiNote When dealing when NBT lists, each element can really be anything. So throw this if, for example, you
     *          expect a list of booleans, but encounter a string, by passing in a {@link UnexpectedNbtTypeException}.
     *          Or pass in a {@link NullPointerException} if the element is somehow null.
     */
    public InvalidNbtValue(
        final @NotNull Throwable cause
    ) {
        super(cause);
    }

    public InvalidNbtValue(
        final @NotNull String message
    ) {
        super(message);
    }
}
