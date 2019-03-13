package vg.civcraft.mc.citadel;

import org.bukkit.Location;

public class ChunkPair {

	private int x;
	private int z;

	public ChunkPair(int x, int z) {
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

	public static ChunkPair fromKey(long key) {
		int x = (int) key;
		int z = (int) ((key >> 32) & 0x00000000);
		return new ChunkPair(x, z);
	}
	
	public static ChunkPair forLocation(Location loc) {
		return new ChunkPair(loc.getChunk().getX(), loc.getChunk().getZ());
	}
	
	public int hashCode() {
		//might collide
		return x & (z << 16);
	}

	public boolean equals(Object o) {
		if (o instanceof ChunkPair) {
			ChunkPair pair = (ChunkPair) o;
			return pair.x == x && pair.z == z;
		}
		return false;
	}
}
