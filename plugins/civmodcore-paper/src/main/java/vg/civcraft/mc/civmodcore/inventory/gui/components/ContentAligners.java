package vg.civcraft.mc.civmodcore.inventory.gui.components;

import java.util.function.Function;

public class ContentAligners {

	public static ContentAligner getLeftAligned() {
		return new Counter(i -> i + 1, 0);
	}

	public static ContentAligner getCentered() {
		return getCentered(9);
	}

	public static ContentAligner getCenteredInOrder(int contentAmount, int windowSize) {
		return getCenteredInOrder(contentAmount, windowSize, 9);
	}

	public static ContentAligner getRightAligned() {
		return getRightAligned(9);
	}

	public static ContentAligner getCenteredInOrder(int contentAmount, int windowSize, int rowLength) {
		int defaultNum;
		if (contentAmount >= windowSize) {
			defaultNum = 0;
		} else {
			int rows = contentAmount / rowLength;
			if (contentAmount % rowLength != 0) {
				rows++;
			}
			int availableRows = windowSize / rowLength;
			if (windowSize % rowLength != 0) {
				availableRows++;
			}
			int emptyRows = availableRows - rows;
			int startingRow = emptyRows / 2;
			if (contentAmount >= rowLength) {
				defaultNum = startingRow * rowLength;
			} else {
				int lengthLastRow = contentAmount - 1;
				int emptySlots = rowLength - lengthLastRow;
				defaultNum = startingRow * rowLength + emptySlots / 2;
			}
		}
		// integer division, so no you can't simplify this
		int offset = 0;
		if (contentAmount % rowLength == 0) {
			offset = 1;
		}
		int lastElementLastCompleteRow = (((contentAmount / rowLength) + offset) * rowLength - 1) + rowLength;

		return new Counter(i -> {
			// just increment until we reach the last element in the last full row
			if (i != lastElementLastCompleteRow) {
				return i + 1;
			} else {
				// jump to offset start of last row
				int lengthLastRow = (contentAmount + rowLength) - lastElementLastCompleteRow - 1;
				int emptySlots = rowLength - lengthLastRow;
				int leftOffset = Math.max(1, emptySlots / 2);
				return (i + 1) + leftOffset;
			}
		}, defaultNum);
	}

	public static ContentAligner getCentered(int rowLength) {
		return new Counter(i -> {
			int rawMiddle = rowLength / 2;
			int rowOffset = i % rowLength;
			if (rowOffset == rowLength - 1) {
				// last one in current row, always at the right end, so step to the center of
				// the next row
				return i + rawMiddle + 1;
			}
			if (rowOffset == rawMiddle) {
				// first step from center is always to the left
				return i - 1;
			}
			if (rowOffset < rawMiddle) {
				// mirror over middle from the left to the right with same relative offset to
				// the center
				return i + 2 * (rawMiddle - rowOffset);
			}
			// mirror over middle from the right to left and increase offset relative to
			// center by one
			return i + 1 + 2 * (rawMiddle - rowOffset);

		}, rowLength / 2);
	}

	public static ContentAligner getRightAligned(int rowLength) {
		return new Counter(i -> {
			if (i % rowLength == 0) {
				// start of row, jump to end of next line
				return i + 2 * rowLength - 1;
			}
			return i - 1;

		}, rowLength - 1);
	}

	private static class Counter implements ContentAligner {
		private int state;
		private Function<Integer, Integer> step;
		private int defaultState;

		Counter(Function<Integer, Integer> step, int defaultState) {
			this.step = step;
			this.defaultState = defaultState;
			this.state = defaultState;
		}

		@Override
		public int getNext() {
			int temp = this.state;
			state = step.apply(state);
			return temp;
		}

		@Override
		public void reset() {
			this.state = defaultState;
		}
	}

}
