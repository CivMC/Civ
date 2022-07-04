package vg.civcraft.mc.citadel.activity;

import org.bukkit.Chunk;

class ChunkCoord {
	public final short worldId;
	public final int x;
	public final int z;

	public ChunkCoord(short worldId, int x, int z) {
		this.worldId = worldId;
		this.x = x;
		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof ChunkCoord key
				&& worldId == key.worldId
				&& x == key.x
				&& z == key.z;
	}

	@Override
	public int hashCode() {
		return (int)worldId & 0xffff | (x << 4) | (z << 18);
	}
}
