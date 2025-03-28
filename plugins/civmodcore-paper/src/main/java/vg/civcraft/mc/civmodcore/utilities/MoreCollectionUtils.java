package vg.civcraft.mc.civmodcore.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import org.apache.commons.collections4.list.LazyList;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Utility class that contains various helpful methods not found on {@link java.util.Collections}, {@link org.apache.commons.collections4.CollectionUtils},
 * and {@link com.google.common.collect.Collections2}, {@link com.google.common.collect.Lists}, {@link com.google.common.collect.Iterables},
 * {@link org.apache.commons.collections4.IterableUtils} and {@link org.apache.commons.collections4.IteratorUtils}, etc.
 *
 * @author Protonull
 */
public final class MoreCollectionUtils {
    /**
     * Creates a modifiable but fixed-length list backed by an Object array.
     *
     * @apiNote Using an Object array <i>should</i> be fine, it's what {@link ArrayList#ArrayList(int)} uses. But this
     *          may need to be revisited when Project Valhalla gets merged in approx 100 years.
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull List<T> newFixedSizeList(
        final int size
    ) {
        return (List<T>) Arrays.asList(new Object[size]);
    }

    /**
     * Retrieves a random element from a list. Will return null if the given list is null or empty, or if the retrieved
     * element is itself null. Best to use this with {@link java.util.Objects#requireNonNullElse(Object, Object)}.
     *
     * @apiNote The idea here is that a random number is only generated when necessary; that if the list only has a
     *          single element, then we needn't waste cycles divining which element to return.
     */
    @Contract("null, _ -> null")
    public static <T> @Nullable T randomElement(
        final List<T> list,
        final @NotNull RandomGenerator random
    ) {
        if (list == null) {
            return null;
        }
        final int size = list.size();
        return switch (size) {
            case 0 -> null;
            case 1 -> list.get(0); // Ignore highlighter: .getFirst() does a size check, and we've already done that
            default -> list.get(random.nextInt(size));
        };
    }

    /**
     * Converts a given list of suppliers into an unmodifiable list of lazily-supplied elements. Each supplier will be
     * called once, if at all.
     */
    public static <T> @NotNull List<T> asLazyList(
        final @NotNull List<@NotNull Supplier<T>> supplierList
    ) {
        final var suppliersCopy = List.copyOf(supplierList);
        return Collections.unmodifiableList(LazyList.lazyList(
            newFixedSizeList(suppliersCopy.size()),
            (i) -> suppliersCopy.get(i).get()
        ));
    }
}
