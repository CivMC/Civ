package vg.civcraft.mc.civmodcore.utilities;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.random.RandomGenerator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.list.LazyList;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class that fills in the gaps of {@link CollectionUtils}.
 *
 * @author Protonull
 */
public final class MoreCollectionUtils {
    /**
     * Creates a new collection with a given set of predefined elements, if any are given.
     */
    @SafeVarargs
    public static <T, C extends Collection<T>> @NotNull C collect(
        final @NotNull IntFunction<@NotNull C> constructor,
        final T ... elements
    ) {
        if (ArrayUtils.isEmpty(elements)) {
            return constructor.apply(0);
        }
        final C collection = constructor.apply(elements.length);
        collection.addAll(Arrays.asList(elements));
        return collection;
    }

    /**
     * Convenience function that creates a mutable but fixed-sized list.
     */
    @SuppressWarnings("unchecked")
    public static <T> @NotNull List<T> newListOfFixedSize(
        final int size
    ) {
        return (List<T>) Arrays.asList(new Object[size]);
    }

    /**
     * Retrieves a random element from a list. Will return null if the given list is null or empty, or if the retrieved
     * element is itself null.
     */
    @Contract("null, _ -> null")
    public static <T> T randomElement(
        final List<T> list,
        final @NotNull RandomGenerator random
    ) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        final int size = list.size();
        if (size == 1) {
            return list.getFirst();
        }
        return list.get(random.nextInt(size));
    }

    /**
     * Converts a given list of suppliers into an unmodifiable list of memos, where each supplier is only called once
     * and then cached for any later re-retrieval.
     */
    public static <T> @NotNull List<T> asLazyList(
        final @NotNull List<@NotNull Supplier<T>> supplierList
    ) {
        final var suppliersCopy = List.copyOf(supplierList);
        return Collections.unmodifiableList(LazyList.lazyList(
            newListOfFixedSize(suppliersCopy.size()),
            (i) -> suppliersCopy.get(i).get()
        ));
    }
}
