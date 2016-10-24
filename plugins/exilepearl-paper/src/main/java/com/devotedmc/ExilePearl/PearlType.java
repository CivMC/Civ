package com.devotedmc.ExilePearl;

public enum PearlType {
	EXILE(0, "Exile Pearl"),
	PRISON(1, "Prison Pearl");

	private final int value;
	private final String title;

	PearlType(int value, String title) {
		this.value = value;
		this.title = title;
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
	
	public String getTitle() {
		return title;
	}
}
