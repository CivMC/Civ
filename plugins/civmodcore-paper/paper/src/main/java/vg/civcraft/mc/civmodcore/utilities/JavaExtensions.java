package vg.civcraft.mc.civmodcore.utilities;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import lombok.experimental.ExtensionMethod;
import lombok.experimental.UtilityClass;

/**
 * Set of extension methods to make Java more tolerable. Use {@link ExtensionMethod @ExtensionMethod} to take most
 * advantage of this.
 */
@UtilityClass
public final class JavaExtensions {

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param fallback The fallback instance to use if "self" is missing.
	 * @return Returns the instance, or the fallback.
	 */
	public static <T> T orElse(final T self, final T fallback) {
		return self == null ? fallback : self;
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param getter The fallback supplier to use if "self" is missing.
	 * @return Returns the instance, or generates a fallback.
	 */
	public static <T> T orElseGet(final T self, final Supplier<T> getter) {
		return self == null ? getter.get() : self;
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @return Returns the instance.
	 *
	 * @throws NullPointerException Throws if the given instance is null.
	 */
	@Nonnull
	public static <T> T isRequired(final T self) {
		return Objects.requireNonNull(self);
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param message The message for {@link NullPointerException} should the instance be null.
	 * @return Returns the instance.
	 *
	 * @throws NullPointerException Throws if the given instance is null.
	 */
	@Nonnull
	public static <T> T isRequired(final T self, @Nonnull final String message) {
		return Objects.requireNonNull(self, Objects.requireNonNull(message, "really?"));
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @return Returns an {@link Optional} wrapping the instance.
	 */
	@Nonnull
	public static <T> Optional<T> asOptional(final T self) {
		return Optional.ofNullable(self);
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param tester The instance tester.
	 * @return Returns the instance if the test passes.
	 *
	 * @throws IllegalStateException This is thrown if the test is failed.
	 */
	@Nonnull
	public static <T> T testCompliance(final T self,
									   @Nonnull final Predicate<T> tester) {
		if (tester.test(self)) {
			return self;
		}
		throw new IllegalStateException("Instance was found to be non-compliant!");
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param tester The instance tester.
	 * @param message The message to use if the test fails.
	 * @return Returns the instance if the test passes.
	 *
	 * @throws IllegalStateException This is thrown if the test is failed.
	 */
	@Nonnull
	public static <T> T testCompliance(final T self,
									   @Nonnull final Predicate<T> tester,
									   @Nonnull final String message) {
		if (tester.test(self)) {
			return self;
		}
		throw new IllegalStateException(Objects.requireNonNull(message, "really?"));
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param defaultString The string to use if the instance is null.
	 * @return Returns a stringified version of the instance, or the default string.
	 */
	@Nonnull
	public static <T> String toStringOr(final T self,
										@Nonnull final String defaultString) {
		if (self == null) {
			return Objects.requireNonNull(defaultString);
		}
		return self.toString();
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param formatString The format string for the stringified instance to be formatted into.
	 * @param defaultString The string to use if the instance is null.
	 * @return Returns a formatted stringified version of the instance, or the default string.
	 *
	 * @see String#format(String, Object...)
	 */
	@Nonnull
	public static <T> String toFormattedString(final T self,
											   @Nonnull final String formatString,
											   @Nonnull final String defaultString) {
		if (self == null) {
			return Objects.requireNonNull(defaultString);
		}
		return String.format(Objects.requireNonNull(formatString), self);
	}

	/**
	 * @param <T> The type of the instance.
	 * @param self The instance itself.
	 * @param formatString The format string for the stringified instance to be formatted into.
	 * @return Returns a formatted stringified version of the instance.
	 *
	 * @see String#format(String, Object...)
	 */
	@Nonnull
	public static <T> String toFormattedString(final T self,
											   @Nonnull final String formatString) {
		return String.format(Objects.requireNonNull(formatString), Objects.requireNonNull(self));
	}

}
