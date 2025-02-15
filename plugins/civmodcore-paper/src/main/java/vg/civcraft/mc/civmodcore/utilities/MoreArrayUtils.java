package vg.civcraft.mc.civmodcore.utilities;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class that fills in any gaps of {@link java.util.Arrays}, {@link org.apache.commons.lang3.ArrayUtils}, and
 * {@link com.google.common.collect.ObjectArrays}.
 *
 * @author Protonull
 */
public final class MoreArrayUtils {
    @FunctionalInterface
    public interface ArrayElementCloner<T extends Cloneable> {
        @Contract("_ -> new")
        @NotNull T cloneElement(
            @NotNull T element
        );
    }

    /**
     * Clones an object array and its elements.
     *
     * @param elementCloner This should almost always be {@link T}'s {@link Object#clone()} implementation.
     */
    public static <T extends Cloneable> T @NotNull [] cloneArrayAndElements(
        T @NotNull [] array,
        final @NotNull ArrayElementCloner<T> elementCloner
    ) {
        array = array.clone();
        for (int i = 0; i < array.length; i++) {
            final T element = array[i];
            if (element != null) {
                array[i] = elementCloner.cloneElement(element);
            }
        }
        return array;
    }
}
