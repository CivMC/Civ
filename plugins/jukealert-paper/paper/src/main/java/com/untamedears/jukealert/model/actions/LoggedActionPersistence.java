package com.untamedears.jukealert.model.actions;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Location;

/**
 * <p>Produced by actions to encapsulate saving them to the database.</p>
 *
 * <p>This class has been prepared to be converted to a Record.</p>
 */
public final class LoggedActionPersistence {

	private final long timestamp;
	private final UUID actorUUID;
	private final int locationX;
	private final int locationY;
	private final int locationZ;
	private final String extra;

	/**
	 * @param timestamp The time (as a UNIX timestamp) when the action took place.
	 * @param actorUUID The UUID of the player who acted.
	 * @param locationX The X location of the action.
	 * @param locationY The Y location of the action.
	 * @param locationZ The Z location of the action.
	 * @param extra Any additional data about the action.
	 */
	public LoggedActionPersistence(final long timestamp,
								   @Nonnull final UUID actorUUID,
								   final int locationX,
								   final int locationY,
								   final int locationZ,
								   final String extra) {
		this.timestamp = timestamp;
		this.actorUUID = Objects.requireNonNull(actorUUID);
		this.locationX = locationX;
		this.locationY = locationY;
		this.locationZ = locationZ;
		this.extra = extra;
		if (StringUtils.length(extra) >= 255) {
			throw new IllegalArgumentException("Action's extra data [" + extra + "] is too long!");
		}
	}

	/**
	 * @deprecated Use {@link #LoggedActionPersistence(long, UUID, int, int, int, String)} instead.
	 */
	@Deprecated
	public LoggedActionPersistence(@Nonnull final UUID actorUUID,
								   final Location location,
								   final long timestamp,
								   final String extra) {
		this(timestamp,
				actorUUID,
				location == null ? 0 : location.getBlockX(),
				location == null ? 0 : location.getBlockY(),
				location == null ? 0 : location.getBlockZ(),
				extra);
	}

	/**
	 * @return Returns the time (as a UNIX timestamp) when the action took place.
	 */
	public long timestamp() {
		return this.timestamp;
	}

	/**
	 * @deprecated Use {@link #timestamp()} instead.
	 */
	@Deprecated
	public long getTime() {
		return this.timestamp;
	}

	/**
	 * @return Returns the UUID of the player who acted.
	 */
	public UUID actorUUID() {
		return this.actorUUID;
	}

	/**
	 * @deprecated Use {@link #actorUUID()} instead.
	 */
	@Deprecated
	public UUID getPlayer() {
		return this.actorUUID;
	}

	/**
	 * @return Returns the X location of the action.
	 */
	public int locationX() {
		return this.locationX;
	}

	/**
	 * @deprecated Use {@link #locationX()} instead.
	 */
	@Deprecated
	public int getX() {
		return this.locationX;
	}

	/**
	 * @return Returns the Y location of the action.
	 */
	public int locationY() {
		return this.locationY;
	}

	/**
	 * @deprecated Use {@link #locationY()} instead.
	 */
	@Deprecated
	public int getY() {
		return this.locationY;
	}

	/**
	 * @return Returns the Z location of the action.
	 */
	public int locationZ() {
		return this.locationZ;
	}

	/**
	 * @deprecated Use {@link #locationZ()} instead.
	 */
	@Deprecated
	public int getZ() {
		return this.locationZ;
	}

	/**
	 * @deprecated Use constituent methods {@link #locationX()}, {@link #locationY()}, and {@link #locationZ()} instead.
	 */
	@Deprecated
	public Location getLocation() {
		return new Location(null, this.locationX, this.locationY, this.locationZ);
	}

	/**
	 * @return Returns this action's additional data, or null.
	 */
	@Nullable
	public String extra() {
		return this.extra;
	}

	/**
	 * @deprecated Use {@link #extra()} instead.
	 */
	@Deprecated
	@Nullable
	public String getVictim() {
		return this.extra;
	}

}
