package com.untamedears.itemexchange.utility.functional;

import java.util.function.Supplier;
import javax.annotation.Nonnull;

/**
 * This is a version of Supplier with the contract of not returning null.
 */
@FunctionalInterface
public interface NonNullSupplier<T> extends Supplier<T> {

	@Nonnull
	@Override
	T get();

}
