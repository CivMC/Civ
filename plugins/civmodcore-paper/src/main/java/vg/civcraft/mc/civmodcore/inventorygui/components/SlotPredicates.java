package vg.civcraft.mc.civmodcore.inventorygui.components;

import java.util.function.IntPredicate;

public final class SlotPredicates {
	
	private SlotPredicates() {
	}
	
	public static IntPredicate slots(int count) {
		Counter counter = new Counter(count);
		return counter::isValid;
	}
	
	public static IntPredicate oneRow() {
		Counter counter = new Counter(9);
		return counter::isValid;
	}
	
	public static IntPredicate twoRows() {
		Counter counter = new Counter(9 * 2);
		return counter::isValid;
	}
	
	public static IntPredicate threeRows() {
		Counter counter = new Counter(9 * 3);
		return counter::isValid;
	}
	
	public static IntPredicate fourRows() {
		Counter counter = new Counter(9 * 4);
		return counter::isValid;
	}
	
	public static IntPredicate fiveRows() {
		Counter counter = new Counter(9 * 5);
		return counter::isValid;
	}

	
	private static class Counter {
		private int count;

		Counter(int count) {
			this.count = count;
		}

		public boolean isValid(int index) {
			return count --> 0;
		}
	}
}
