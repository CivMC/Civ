package vg.civcraft.mc.civmodcore.world;

import com.google.common.base.Objects;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.World;
import vg.civcraft.mc.civmodcore.api.LocationAPI;

public class WorldXZ {

	private final UUID world;
	private final int x;
	private final int z;

	public WorldXZ(final Location location) {
		if (!LocationAPI.isValidLocation(location)) {
			throw new IllegalArgumentException("Location must not be valid!");
		}
		final World world = location.getWorld();
		this.world = world.getUID(); // Do not listen to the highlighter
		this.x = location.getBlockX();
		this.z = location.getBlockZ();
	}

	public WorldXZ(final World world, final int x, final int z) {
		if (world == null) {
			throw new IllegalArgumentException("World must not be null!");
		}
		this.world = world.getUID();
		this.x = x;
		this.z = z;
	}

	public WorldXZ(final UUID world, final int x, final int z) {
		if (world == null) {
			throw new IllegalArgumentException("World UUID must not be null!");
		}
		this.world = world;
		this.x = x;
		this.z = z;
	}

	public UUID getWorld() {
		return this.world;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		}
		if (!(object instanceof WorldXZ)) {
			return false;
		}
		final WorldXZ other = (WorldXZ) object;
		return Objects.equal(this.world, other.world)
				&& this.x == other.x
				&& this.z == other.z;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.world, this.x, this.z);
	}

	public static WorldXZ fromLocation(final Location location) {
		if (!LocationAPI.isValidLocation(location)) {
			throw new IllegalArgumentException("Location cannot be null!");
		}
		final World world = location.getWorld();
		final int x = location.getBlockX();
		final int z = location.getBlockZ();
		return new WorldXZ(world.getUID(), x, z); // Do not listen to the highlighter
	}

}
