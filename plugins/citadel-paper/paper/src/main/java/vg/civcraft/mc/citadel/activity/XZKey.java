package vg.civcraft.mc.citadel.activity;

class XZKey {
	private final int x;
	private final int z;

	public XZKey(int x, int z) {
		this.x = x;
		this.z = z;
	}

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
