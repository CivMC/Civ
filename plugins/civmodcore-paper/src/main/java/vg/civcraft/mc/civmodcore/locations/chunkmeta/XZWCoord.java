package vg.civcraft.mc.civmodcore.locations.chunkmeta;

import java.util.Objects;

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
	public int compareTo(XZWCoord o) {
		int worldComp = Short.compare(this.worldID, o.getWorldID());
		if (worldComp != 0) {
			return worldComp;
		}
		int xComp = Integer.compare(this.x, o.getX());
		if (xComp != 0) {
			return worldComp;
		}
		return Integer.compare(this.z, o.getZ());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, z, worldID);
	}

}
