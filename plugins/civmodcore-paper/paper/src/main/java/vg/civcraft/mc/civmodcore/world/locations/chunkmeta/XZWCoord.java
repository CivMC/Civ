package vg.civcraft.mc.civmodcore.world.locations.chunkmeta;

import java.util.Objects;
import org.bukkit.Chunk;
import org.bukkit.Location;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.world.locations.chunkmeta.block.BlockBasedChunkMeta;

public class XZWCoord implements Comparable<XZWCoord> {

	/**
	 * Chunk x-coord
	 */
	protected int x;

	/**
	 * Chunk z-coord
	 */
	protected int z;

	/**
	 * Internal ID of the world the chunk is in
	 */
	protected short worldID;

	public XZWCoord(int x, int z, short worldID) {
		this.x = x;
		this.z = z;
		this.worldID = worldID;
	}

	/**
	 * @return Internal ID of the world this chunk is in
	 */
	public short getWorldID() {
		return worldID;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public String toString() {
		return String.format("(%d, %d):%d", x, z, worldID);
	}

	@Override
	public int compareTo(XZWCoord o) {
		int worldComp = Short.compare(this.worldID, o.getWorldID());
		if (worldComp != 0) {
			return worldComp;
		}
		int xComp = Integer.compare(this.x, o.getX());
		if (xComp != 0) {
			return xComp;
		}
		return Integer.compare(this.z, o.getZ());
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, z, worldID);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof XZWCoord)) {
			return false;
		}
		XZWCoord xzwCoord = (XZWCoord) o;
		return x == xzwCoord.x && z == xzwCoord.z && worldID == xzwCoord.worldID;
	}

	public static XZWCoord fromLocation(Location location, short worldID) {
		return new XZWCoord(BlockBasedChunkMeta.toChunkCoord(location.getBlockX()),
				BlockBasedChunkMeta.toChunkCoord(location.getBlockZ()), worldID);
	}

	public static XZWCoord fromLocation(Location location) {
		short worldId = CivModCorePlugin.getInstance().getWorldIdManager().getInternalWorldId(location.getWorld());
		return fromLocation(location, worldId);
	}

	public static XZWCoord fromChunk(Chunk chunk) {
		short worldId = CivModCorePlugin.getInstance().getWorldIdManager().getInternalWorldId(chunk.getWorld());
		return new XZWCoord(chunk.getX(), chunk.getZ(), worldId);
	}

}
