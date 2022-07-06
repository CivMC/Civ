package vg.civcraft.mc.citadel.activity;

record XZKey (int x, int z) {
	@Override
	public boolean equals(Object obj) {
		return obj instanceof XZKey key
				&& x == key.x
				&& z == key.z;
	}

	@Override
	public int hashCode() {
		return x | (z << 8);
	}
}
