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

	public static PearlType valueOf(Integer value) {
		if (value == null) {
			return null;
		}
		
		switch(value) {
		case 0:
			return EXILE;
		case 1:
			return PRISON;
		default:
			return null;
		}
	}
	
	public static PearlType fromString(String value) {
		if (value == null) {
			return null;
		}
		
		switch(value.toLowerCase()) {
		case "exile":
			return EXILE;
		case "prison":
			return PRISON;
		default:
			return null;
		}
	}
	
	public String getTitle() {
		return title;
	}
}
