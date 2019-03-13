package vg.civcraft.mc.citadel.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkCoord {

	private int x;
	private int z;

	public ChunkCoord(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	public long getCombinedKey() {
		long key = x;
		long zPart = z;
		key &= zPart << 32;
		return key;
	}

	public static ChunkCoord fromKey(long key) {
		int x = (int) key;
		int z = (int) ((key >> 32) & 0x00000000);
		return new ChunkCoord(x, z);
	}
	
	public static ChunkCoord forLocation(Location loc) {
		return new ChunkCoord(loc.getChunk().getX(), loc.getChunk().getZ());
	}
	
	public static ChunkCoord forChunk(Chunk chunk) {
		return new ChunkCoord(chunk.getX(), chunk.getZ());
	}
	
	public int hashCode() {
		//might collide
		return x & (z << 16);
	}

	public boolean equals(Object o) {
		if (o instanceof ChunkCoord) {
			ChunkCoord pair = (ChunkCoord) o;
			return pair.x == x && pair.z == z;
		}
		return false;
	}
}
