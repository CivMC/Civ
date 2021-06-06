package vg.civcraft.mc.civmodcore.inventorygui.components;

import java.util.function.IntPredicate;

public final class SlotPredicates {

	private static final int ROW_LENGTH = 9;

	private SlotPredicates() {
	}

	public static IntPredicate slots(int count) {
		Counter counter = new Counter(count);
		return counter::isValid;
	}

	public static IntPredicate rows(int height) {
		return rows(height, 9);
	}

	public static IntPredicate rows(int height, int rowLength) {
		return slots(height * rowLength);
	}

	public static IntPredicate offsetRectangle(int height, int width, int y, int x,
			int rowLength) {
		OffsetSlotter slotter = new OffsetSlotter(y, x, height, width, rowLength);
		return slotter::isValid;
	}

	public static IntPredicate offsetRectangle(int height, int width, int y, int x) {
		return offsetRectangle(height, width, y, x, ROW_LENGTH);
	}

	public static IntPredicate rectangle(int height, int width, int rowLength) {
		return offsetRectangle(height, width, 0, 0, rowLength);
	}

	public static IntPredicate rectangle(int height, int width) {
		return rectangle(height, width, ROW_LENGTH);
	}

	private static class OffsetSlotter {

		private int lowerHorizontalBound;
		private int lowerVerticalBound;
		private int upperHorizontalBound;
		private int upperVerticalBound;
		private int rowLength;

		OffsetSlotter(int y, int x, int height, int width, int rowLength) {
			this.lowerHorizontalBound = x;
			this.upperHorizontalBound = x + width - 1;
			this.lowerVerticalBound = y;
			this.upperVerticalBound = y + height - 1;
			this.rowLength = rowLength;
		}

		boolean isValid(int index) {
			int row = index / rowLength;
			int column = index % rowLength;
			return column >= lowerHorizontalBound && column <= upperHorizontalBound && row >= lowerVerticalBound
					&& row <= upperVerticalBound;
		}

	}

	private static class Counter {
		private int count;

		Counter(int count) {
			this.count = count;
		}

		public boolean isValid(int index) {
			return count-- > 0;
		}
	}

}
