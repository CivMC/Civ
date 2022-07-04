package vg.civcraft.mc.citadel.activity;

class RegionCoord {
	public final short worldId;
	public final int x;
	public final int z;

	public RegionCoord(short worldId, int x, int z) {
		this.worldId = worldId;
		this.x = x;
		this.z = z;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof RegionCoord coord
				&& worldId == coord.worldId
				&& x == coord.x
				&& z == coord.z;
	}

	@Override
	public int hashCode() {
		return (int)worldId & 0xffff | (x << 8) | (z << 16);
	}
}
