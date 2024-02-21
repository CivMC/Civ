package com.untamedears.itemexchange.utility;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.functional.NonNullSupplier;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A storage method for modifiers that puts significance on the modifier's class.
 */
public final class ModifierStorage implements Iterable<ModifierData> {

	private final ClassToInstanceMap<ModifierData> map = MutableClassToInstanceMap.create();

	/**
	 * @return Returns how many modifiers are being stored.
	 */
	public int size() {
		return this.map.size();
	}

	/**
	 * @return Returns true if no modifiers are being stored.
	 */
	public boolean isEmpty() {
		return this.map.size() <= 0;
	}

	/**
	 * Removes all modifier from storage.
	 */
	public void clear() {
		this.map.clear();
	}

	/**
	 * Determines whether a particular modifier class is being stored.
	 *
	 * @param clazz The class of the modifier.
	 * @return Returns true if the modifier is stored.
	 */
	public boolean hasModifier(@Nullable final Class<? extends ModifierData> clazz) {
		return clazz != null && this.map.containsKey(clazz);
	}

	/**
	 * Determines whether a particular modifier is being stored.
	 *
	 * @param instance The instance of the modifier.
	 * @return Returns true if the modifier is stored.
	 */
	public boolean hasModifier(@Nullable final ModifierData instance) {
		return instance != null && this.map.containsKey(instance.getClass());
	}

	/**
	 * Attempts to retrieve a modifier from storage.
	 *
	 * @param <T> The type of the modifier.
	 * @param clazz The class of the modifier.
	 * @return Returns a cast modifier to the given type, or null.
	 */
	@Nullable
	public <T extends ModifierData> T get(@Nonnull final Class<T> clazz) {
		return (T) this.map.getInstance(Objects.requireNonNull(clazz));
	}

	/**
	 * Attempts to retrieve a modifier from storage.
	 *
	 * @param <T> The type of the modifier.
	 * @param instance An example instance of the modifier.
	 * @return Returns a cast modifier to the given type, or null.
	 */
	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T get(@Nonnull final T instance) {
		return get((Class<T>) instance.getClass());
	}

	/**
	 * Attempts to retrieve a modifier from storage. If that modifier is missing, the given supplier will be used to
	 * generate and store a new instance of that modifier.
	 *
	 * @param <T> The type of the modifier.
	 * @param clazz The class of the modifier.
	 * @param supplier A supplier to create a new modifier. MUST NOT RETURN NULL!
	 * @return Returns a cast modifier to the given type, or null if the given supplier returned null.
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T getOrDefault(@Nonnull final Class<T> clazz,
												   @Nonnull final NonNullSupplier<T> supplier) {
		return (T) this.map.computeIfAbsent(Objects.requireNonNull(clazz), (_clazz) -> supplier.get());
	}

	/**
	 * Attempts to retrieve a modifier from storage. If that modifier is missing, the given supplier will be used to
	 * generate and store a new instance of that modifier.
	 *
	 * @param <T> The type of the modifier.
	 * @param instance An example instance of the modifier.
	 * @param supplier A supplier to create a new modifier. MUST NOT RETURN NULL!
	 * @return Returns a cast modifier to the given type, or null if the given supplier returned null.
	 */
	@Nonnull
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T getOrDefault(@Nonnull final T instance,
												   @Nonnull final NonNullSupplier<T> supplier) {
		return getOrDefault((Class<T>) instance.getClass(), supplier);
	}

	/**
	 * Adds a modifier to storage.
	 *
	 * @param instance The modifier to add to storage.
	 * @return Returns any previous modifier that was stored, not null.
	 */
	@Nullable
	public ModifierData put(@Nonnull final ModifierData instance) {
		return this.map.put(instance.getClass(), instance);
	}

	/**
	 * Removes a modifier from storage.
	 *
	 * @param clazz The modifier class to remove.
	 * @return Returns what was removed.
	 */
	@Nullable
	public ModifierData remove(@Nullable final Class<ModifierData> clazz) {
		return this.map.remove(clazz);
	}

	/**
	 * Removes a modifier from storage.
	 *
	 * @param instance The modifier to remove.
	 * @return Returns what was removed.
	 */
	@Nullable
	public ModifierData remove(@Nullable final ModifierData instance) {
		return instance == null ? null : this.map.remove(instance.getClass());
	}

	@Override
	public String toString() {
		return "ModifierStorage" + this.map.values();
	}

	@Nonnull
	@Override
	public Iterator<ModifierData> iterator() {
		return this.map.values().iterator();
	}

	@Nonnull
	@Override
	public Spliterator<ModifierData> spliterator() {
		return this.map.values().spliterator();
	}

	@Nonnull
	public Stream<ModifierData> stream() {
		return this.map.values().stream()
				.filter(Objects::nonNull)
				.sorted();
	}

	@Override
	public void forEach(@Nonnull final Consumer<? super ModifierData> action) {
		stream().forEachOrdered(action);
	}

}
