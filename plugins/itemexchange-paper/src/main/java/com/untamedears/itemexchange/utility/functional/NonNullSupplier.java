package com.untamedears.itemexchange.utility.functional;

import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;

/**
 * This is a version of Supplier with the contract of not returning null.
 */
@FunctionalInterface
public interface NonNullSupplier<T> extends Supplier<T> {

    @NotNull
    @Override
    T get();

}
