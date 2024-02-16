package vg.civcraft.mc.citadel.activity;

record ChunkCoord (short worldId, int x, int z) {
	@Override
	public int hashCode() {
		return (int)worldId & 0xffff | (x << 4) | (z << 18);
	}
}
