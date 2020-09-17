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

	public static IntPredicate rows(int rows) {
		return rows(rows, 9);
	}

	public static IntPredicate rows(int rows, int rowLength) {
		return slots(rows * rowLength);
	}

	public static IntPredicate offsetRectangle(int rows, int columns, int verticalOffset, int horizontalOffset,
			int rowLength) {
		OffsetSlotter slotter = new OffsetSlotter(verticalOffset, horizontalOffset, rows, columns, rowLength);
		return slotter::isValid;
	}

	public static IntPredicate offsetRectangle(int rows, int columns, int verticalOffset, int horizontalOffset) {
		return offsetRectangle(rows, columns, verticalOffset, horizontalOffset, ROW_LENGTH);
	}

	public static IntPredicate rectangle(int rows, int columns, int rowLength) {
		return offsetRectangle(rows, columns, 0, 0, rowLength);
	}

	public static IntPredicate rectangle(int rows, int columns) {
		return rectangle(rows, columns, ROW_LENGTH);
	}

	private static class OffsetSlotter {

		private int lowerHorizontalBound;
		private int lowerVerticalBound;
		private int upperHorizontalBound;
		private int upperVerticalBound;
		private int rowLength;

		OffsetSlotter(int offSetRow, int offSetColumn, int sizeRows, int sizeColumns, int rowLength) {
			this.lowerHorizontalBound = offSetColumn;
			this.upperHorizontalBound = offSetColumn + sizeColumns - 1;
			this.lowerVerticalBound = offSetRow;
			this.upperVerticalBound = offSetRow + sizeRows - 1;
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
