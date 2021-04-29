package vg.civcraft.mc.civmodcore.util;

import java.nio.ByteBuffer;
import java.util.UUID;
import org.apache.commons.lang3.ArrayUtils;

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
	public static boolean isNullOrIdentity(final UUID uuid) {
		return uuid == null || IDENTITY.equals(uuid);
	}

	/**
	 * Attempts to parse a UUID from a given string.
	 *
	 * @param value The string to parse.
	 * @return Returns a valid UUID, or null.
	 */
	public static UUID fromString(final String value) {
		if (value == null) {
			return null;
		}
		try {
			return UUID.fromString(value);
		}
		catch (final Exception ignored) {
			return null;
		}
	}

	/**
	 * Converts a UUID to a byte array.
	 *
	 * @param uuid The UUID to convert, can be null.
	 * @return Returns a 16 byte array representing the UUID.
	 */
	public static byte[] uuidToBytes(final UUID uuid) {
		if (isNullOrIdentity(uuid)) {
			return new byte[16];
		}
		// Creating a new ByteBuffer to almost immediately expend it just
		// to get the bytes is a bit extra, but other ways aren't as clean
		// or readable.
		final ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
		buffer.putLong(uuid.getMostSignificantBits());
		buffer.putLong(uuid.getLeastSignificantBits());
		return buffer.array();
	}

	/**
	 * <p>Converts a given byte array to a UUID.</p>
	 *
	 * <p>Note: You can provide a byte array of any length, or even null. Null or zero length arrays will
	 * return {@link #IDENTITY}. Byte arrays less than 16 elements long will be used with the remainder
	 * inferred as zeroed out bytes. With byte arrays larger than 16 elements, only those first 16
	 * elements are considered.</p>
	 *
	 * @param bytes The byte array to create the UUID from.
	 * @return Returns an instance of UUID based on the given bytes.
	 */
	public static UUID bytesToUUID(byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return IDENTITY;
		}
		if (bytes.length < 16) { // Support shorter UUIDs
			bytes = ArrayUtils.addAll(bytes, new byte[16 - bytes.length]);
		}
		final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		final long high = byteBuffer.getLong();
		final long low = byteBuffer.getLong();
		return new UUID(high, low);
	}

}
