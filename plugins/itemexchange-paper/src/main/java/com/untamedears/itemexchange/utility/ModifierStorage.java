package com.untamedears.itemexchange.utility;

import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

/**
 * A storage method for modifiers that puts significance on the modifier's class.
 */
public final class ModifierStorage implements Iterable<ModifierData> {

	private final Map<Class<ModifierData>, ModifierData> map = new HashMap<>();

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
	public boolean hasModifier(Class<? extends ModifierData> clazz) {
		if (clazz == null) {
			return false;
		}
		return this.map.get(clazz) != null;
	}

	/**
	 * Determines whether a particular modifier is being stored.
	 *
	 * @param instance The instance of the modifier.
	 * @return Returns true if the modifier is stored.
	 */
	public boolean hasModifier(ModifierData instance) {
		if (instance == null) {
			return false;
		}
		return this.map.get(instance.getClass()) != null;
	}

	/**
	 * Attempts to retrieve a modifier from storage.
	 *
	 * @param <T> The type of the modifier.
	 * @param clazz The class of the modifier.
	 * @return Returns a cast modifier to the given type, or null.
	 */
	public <T extends ModifierData> T get(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		return MoreClassUtils.castOrNull(clazz, this.map.get(clazz));
	}

	/**
	 * Attempts to retrieve a modifier from storage.
	 *
	 * @param <T> The type of the modifier.
	 * @param instance An example instance of the modifier.
	 * @return Returns a cast modifier to the given type, or null.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T get(T instance) {
		if (instance == null) {
			return null;
		}
		return get((Class<T>) instance.getClass());
	}

	/**
	 * Attempts to retrieve a modifier from storage. If that modifier is missing, the given supplier will be used to
	 * generate and store a new instance of that modifier.
	 *
	 * @param <T> The type of the modifier.
	 * @param clazz The class of the modifier.
	 * @param supplier A supplier to create a new modifier.
	 * @return Returns a cast modifier to the given type, or null if the given supplier returned null.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T getOrDefault(Class<T> clazz, Supplier<T> supplier) {
		if (clazz == null || supplier == null) {
			return null;
		}
		T current = get(clazz);
		if (current == null) {
			current = supplier.get();
			if (current == null) {
				return null;
			}
			this.map.put((Class<ModifierData>) clazz, current);
		}
		return current;
	}

	/**
	 * Attempts to retrieve a modifier from storage. If that modifier is missing, the given supplier will be used to
	 * generate and store a new instance of that modifier.
	 *
	 * @param <T> The type of the modifier.
	 * @param instance An example instance of the modifier.
	 * @param supplier A supplier to create a new modifier.
	 * @return Returns a cast modifier to the given type, or null if the given supplier returned null.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T getOrDefault(T instance, Supplier<T> supplier) {
		return getOrDefault((Class<T>) instance.getClass(), supplier);
	}

	/**
	 * Adds a modifier to storage.
	 *
	 * @param instance The modifier to add to storage.
	 * @return Returns any previous modifier that was stored, not null.
	 */
	@SuppressWarnings("unchecked")
	public ModifierData put(ModifierData instance) {
		if (instance == null) {
			return null;
		}
		return this.map.put((Class<ModifierData>) instance.getClass(), instance);
	}

	/**
	 * Removes a modifier from storage.
	 *
	 * @param clazz The modifier class to remove.
	 * @return Returns what was removed.
	 */
	public ModifierData remove(Class<ModifierData> clazz) {
		return this.map.remove(clazz);
	}

	/**
	 * Removes a modifier from storage.
	 *
	 * @param instance The modifier to remove.
	 * @return Returns what was removed.
	 */
	public ModifierData remove(ModifierData instance) {
		if (instance == null) {
			return null;
		}
		return this.map.remove(instance.getClass());
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		ModifierStorage other = MoreClassUtils.castOrNull(ModifierStorage.class, object);
		if (other == null) {
			return false;
		}
		if (!this.map.equals(other.map)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 239487509 + this.map.hashCode();
	}

	@Override
	public String toString() {
		return "ModifierStorage" + this.map.values().toString();
	}

	@Override
	public Iterator<ModifierData> iterator() {
		return this.map.values().iterator();
	}

	@Override
	public void forEach(Consumer<? super ModifierData> action) {
		stream().forEachOrdered(action);
	}

	@Override
	public Spliterator<ModifierData> spliterator() {
		return this.map.values().spliterator();
	}

	public Stream<ModifierData> stream() {
		return this.map.values().stream()
				.filter(Objects::nonNull)
				.sorted();
	}

}
