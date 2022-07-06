package vg.civcraft.mc.citadel.activity;

record ChunkCoord (short worldId, int x, int z) {
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
