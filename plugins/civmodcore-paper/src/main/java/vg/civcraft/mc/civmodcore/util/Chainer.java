package vg.civcraft.mc.civmodcore.util;

import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * <p>Utility class that allows for something roughly within the range of Javascript's optional chaining.</p>
 *
 * <p>Read more about that
 * <a href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Operators/Optional_chaining">here</a>.</p>
 *
 * @author Protonull
 */
public final class Chainer<T> {

    private final T value;

    private Chainer(final T value) {
        this.value = value;
    }

    /**
     * @return Returns the current value of the chainer.
     */
    @Nullable
    public T get() {
        return this.value;
    }

    /**
     * @param fallback The given fallback should the current value of the chainer be null.
     * @return Returns the current value of the chainer, or the given fallback if null.
     */
    @Contract("null -> _; !null -> !null")
    public T getOrDefault(final T fallback) {
        if (this.value == null) {
            return fallback;
        }
        return this.value;
    }

	/**
	 * @param generator Generator that creates a new value should the current value of the chainer be null.
	 * @return Returns the current value of the chainer, or the result of the generator if null.
	 */
	@Contract("null -> fail")
	public T getOrGenerate(final Supplier<T> generator) {
		Validate.notNull(generator, "Chainer parser cannot be null!");
		if (this.value == null) {
			return generator.get();
		}
		return this.value;
	}

    /**
     * Continues the chain.
     *
     * @param <V>    The return type of the given parser.
     * @param parser The chainer parser, which can safely assume that the passed in object is not-null.
     * @return Returns a new chainer instance wrapping the result of the parser.
     */
	@Contract("null -> fail; !null -> new")
    public <V> Chainer<V> then(final Function<T, V> parser) {
        Validate.notNull(parser, "Chainer parser cannot be null!");
        if (this.value == null) {
            return new Chainer<>(null);
        }
        return new Chainer<>(parser.apply(this.value));
    }

    /**
     * Creates a new chainer from a given object.
     *
     * @param <T>   The type of the given object.
     * @param value The object to start chaining from.
     * @return Returns a new chainer instance.
     */
	@Contract("_ -> new")
    public static <T> Chainer<T> from(final T value) {
        return new Chainer<>(value);
    }

}
