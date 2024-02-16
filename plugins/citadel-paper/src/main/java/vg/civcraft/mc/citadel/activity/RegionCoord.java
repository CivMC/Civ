package vg.civcraft.mc.citadel.activity;

record RegionCoord (short worldId, int x, int z) {
	@Override
	public int hashCode() {
		return (int)worldId & 0xffff | (x << 8) | (z << 16);
	}
}
