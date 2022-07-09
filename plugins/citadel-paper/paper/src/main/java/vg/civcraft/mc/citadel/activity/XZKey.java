package vg.civcraft.mc.citadel.activity;

record XZKey (int x, int z) {
	@Override
	public int hashCode() {
		return x | (z << 8);
	}
}
