package com.devotedmc.ExilePearl;

public enum PearlType {
	EXILE(0),
	PRISON(1);

	private final int value;

	PearlType(int value) {
		this.value = value;
	}

	public int toInt() {
		return value;
	}

	public static PearlType valueOf(int value) {
		switch(value) {
		case 0:
			return EXILE;
		case 1:
			return PRISON;
		default:
			return null;
		}
	}
}
