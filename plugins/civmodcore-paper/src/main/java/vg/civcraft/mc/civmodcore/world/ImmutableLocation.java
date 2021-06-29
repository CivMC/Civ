package vg.civcraft.mc.civmodcore.world;

import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

public class ImmutableLocation extends Location {

	private static final String IMMUTABLE_LOCATION_ERROR = "This location is immutable";

	private final UUID worldUUID;

	public ImmutableLocation(@Nonnull final Location location) {
		this(location.isWorldLoaded() ? location.getWorld() : null,
				location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}

	public ImmutableLocation(@Nullable final World world,
							 final double x,
							 final double y,
							 final double z) {
		this(world, x, y, z, 0, 0);
	}

	public ImmutableLocation(@Nullable final World world,
							 final double x,
							 final double y,
							 final double z,
							 final float yaw,
							 final float pitch) {
		super(world, x, y, z, yaw, pitch);
		this.worldUUID = world == null ? null : world.getUID();
	}

	public UUID getWorldUUID() {
		return this.worldUUID;
	}

	@Override
	public void setWorld(@Nullable final World world) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Override
	public void setX(final double x) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Override
	public void setY(final double y) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Override
	public void setZ(final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Override
	public void setYaw(final float yaw) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Override
	public void setPitch(final float pitch) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location setDirection(@Nonnull final Vector vector) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location add(@Nonnull final Location location) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location add(@Nonnull final Vector vector) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location add(final double x, final double y, final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location subtract(@Nonnull final Location location) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location subtract(@Nonnull final Vector vector) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	@Override
	public Location subtract(final double x, final double y, final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public Location multiply(final double m) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public Location zero() {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public Location set(final double x, final double y, final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public Location add(@Nonnull final Location base, final double x, final double y, final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public Location subtract(@Nonnull final Location base, final double x, final double y, final double z) {
		throw new NotImplementedException(IMMUTABLE_LOCATION_ERROR);
	}

	@Nonnull
	public static Location deserialize(@Nonnull final Map<String, Object> args) {
		World world = null;
		if (args.containsKey("world")) {
			world = Bukkit.getWorld((String) args.get("world"));
			if (world == null) {
				throw new IllegalArgumentException("unknown world");
			}
		}
		return new ImmutableLocation(world,
				NumberConversions.toDouble(args.get("x")),
				NumberConversions.toDouble(args.get("y")),
				NumberConversions.toDouble(args.get("z")),
				NumberConversions.toFloat(args.get("yaw")),
				NumberConversions.toFloat(args.get("pitch")));
	}

}
