package com.untamedears.realisticbiomes.model;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class ChunkCoord implements Comparable<ChunkCoord> {

	public static ChunkCoord forChunk(Chunk chunk) {
		return new ChunkCoord(chunk.getX(), chunk.getZ());
	}
	public static ChunkCoord forLocation(Location loc) {
		return new ChunkCoord(loc.getChunk().getX(), loc.getChunk().getZ());
	}

	public static ChunkCoord fromKey(long key) {
		int x = (int) key;
		int z = (int) ((key >> 32) & 0x00000000);
		return new ChunkCoord(x, z);
	}

	private int x;

	private int z;

	public ChunkCoord(int x, int z) {
		this.x = x;
		this.z = z;
	}

	@Override
	public int compareTo(ChunkCoord o) {
		int compX = Integer.compare(x, o.x);
		if (compX != 0) {
			return compX;
		}
		return Integer.compare(z, o.z);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ChunkCoord) {
			ChunkCoord pair = (ChunkCoord) o;
			return pair.x == x && pair.z == z;
		}
		return false;
	}

	public long getCombinedKey() {
		long key = x;
		long zPart = z;
		key &= zPart << 32;
		return key;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}
	@Override
	public int hashCode() {
		// might collide
		return x & (z << 16);
	}
}
