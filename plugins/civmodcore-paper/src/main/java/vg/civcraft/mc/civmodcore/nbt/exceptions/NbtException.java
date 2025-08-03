package vg.civcraft.mc.civmodcore.nbt.exceptions;

import org.jetbrains.annotations.NotNull;

public abstract class NbtException extends RuntimeException {
    public NbtException() {
        super();
    }

    public NbtException(
        final @NotNull String message
    ) {
        super(message);
    }

    public NbtException(
        final @NotNull String message,
        final @NotNull Throwable cause
    ) {
        super(message, cause);
    }

    public NbtException(
        final @NotNull Throwable cause
    ) {
        super(cause);
    }
}
