package com.devotedmc.ExilePearl.storage;

/**
 * The type of storage that is used
 * 
 * @author Gordon
 */
public enum StorageType {
	FILE(0),
	MYSQL(1),
	RAM(2);

	private final int num;

	StorageType(int num) {
		this.num = num;
	}

	public static StorageType valueOf(int num) {
		switch(num) {
		case 0:
			return FILE;
		case 1:
			return MYSQL;
		case 2:
			return RAM;
		default:
			return FILE;
		}
	}

	public int getNum() {
		return num;
	}
}
