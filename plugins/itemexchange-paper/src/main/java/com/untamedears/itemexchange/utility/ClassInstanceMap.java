package com.untamedears.itemexchange.utility;

import com.google.common.base.Supplier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import vg.civcraft.mc.civmodcore.util.Iteration;

public class ClassInstanceMap<T> implements Iterable<T> {

	private final Map<Class<T>, T> map = new HashMap<>();

	public int size() {
		return this.map.size();
	}

	public boolean isEmpty() {
		return this.map.size() <= 0;
	}

	public boolean containsClass(Class<T> clazz) {
		if (clazz == null) {
			return false;
		}
		return this.map.containsKey(clazz);
	}

	public boolean containsInstance(T instance) {
		if (instance == null) {
			return false;
		}
		T current = this.map.get(instance.getClass());
		if (current == null) {
			return false;
		}
		return true;
	}

	public T get(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}
		return this.map.get(clazz);
	}

	public T get(T instance) {
		if (instance == null) {
			return null;
		}
		return this.map.get(instance.getClass());
	}

	public T getOrDefault(Class<T> clazz, Supplier<T> constructor) {
		if (clazz == null || constructor == null) {
			return null;
		}
		T current = this.map.get(clazz);
		if (current == null) {
			current = constructor.get();
			if (current == null) {
				return null;
			}
			this.map.put(clazz, current);
		}
		return current;
	}

	public T getOrDefault(T instance, Supplier<T> constructor) {
		if (instance == null || constructor == null) {
			return null;
		}
		T current = this.map.get(instance.getClass());
		if (current == null) {
			current = constructor.get();
			if (current == null) {
				return null;
			}
			put(current);
		}
		return current;
	}

	@SuppressWarnings("unchecked")
	public T put(T instance) {
		if (instance == null) {
			return null;
		}
		return this.map.put((Class<T>) instance.getClass(), instance);
	}

	@SafeVarargs
	public final void setAll(T... instances) {
		this.map.clear();
		if (Iteration.isNullOrEmpty(instances)) {
			return;
		}
		for (T instance : instances) {
			put(instance);
		}
	}

	public T remove(Class<T> clazz) {
		return this.map.remove(clazz);
	}

	public T remove(T instance) {
		if (instance == null) {
			return null;
		}
		return this.map.remove(instance.getClass());
	}

	public void clear() {
		this.map.clear();
	}

	public Set<Class<T>> classes() {
		return this.map.keySet();
	}

	public Collection<T> instances() {
		return this.map.values();
	}

	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (!(other instanceof ClassInstanceMap)) {
			return false;
		}
		return this.map.equals(((ClassInstanceMap<?>) other).map);
	}

	@Override
	public int hashCode() {
		return 239487509 + this.map.hashCode();
	}

	@Override
	public String toString() {
		return "ClassInstanceMap" + this.map.values().toString();
	}

	@Override
	public Iterator<T> iterator() {
		return this.map.values().iterator();
	}

	@Override
	public void forEach(Consumer<? super T> action) {
		stream().forEachOrdered(action);
	}

	@Override
	public Spliterator<T> spliterator() {
		return this.map.values().spliterator();
	}

	public Stream<T> stream() {
		return this.map.values().stream().filter(Objects::nonNull).sorted();
	}

}
