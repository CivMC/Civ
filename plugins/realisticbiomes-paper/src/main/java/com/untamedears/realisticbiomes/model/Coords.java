package com.untamedears.realisticbiomes.model;

import org.bukkit.Location;

class Coords implements Comparable<Coords> {

	private int x;
	private int y;
	private int z;

	Coords(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	Coords(Location loc) {
		this.x = loc.getBlockX();
		this.y = loc.getBlockY();
		this.z = loc.getBlockZ();
	}

	@Override
	public int compareTo(Coords coords) {
		// y first because we have the most variety here
		if (coords.y != y) {
			return Integer.compare(y, coords.y);
		}
		if (coords.x != x) {
			return Integer.compare(x, coords.x);
		}
		if (coords.z != z) {
			return Integer.compare(z, coords.z);
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		Coords coords = (Coords) o;
		return coords.y == y && coords.x == x && coords.z == z;
	}
}
