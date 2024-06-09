package vg.civcraft.mc.civmodcore.utilities;

import java.util.UUID;
import org.jetbrains.annotations.Nullable;

/**
 * Class of utilities relating to UUIDs
 *
 * @author Protonull
 */
public final class UuidUtils {
	public static final UUID IDENTITY = new UUID(0L, 0L);

	/**
	 * Determines whether a given UUID is null or a default 0,0 value.
	 *
	 * @param uuid The UUID to test.
	 * @return Returns true if the given UUID is null or default.
	 */
	public static boolean isNullOrIdentity(
		final UUID uuid
	) {
		return uuid == null || IDENTITY.equals(uuid);
	}

	/**
	 * Attempts to parse a UUID from a given string.
	 *
	 * @param value The string to parse.
	 * @return Returns a valid UUID, or null.
	 */
	public static @Nullable UUID fromString(
		final String value
	) {
		if (value == null) {
			return null;
		}
		try {
			return UUID.fromString(value);
		}
		catch (final IllegalArgumentException ignored) {
			return null;
		}
	}
}
