package vg.civcraft.mc.civmodcore.inventorygui.history;

import java.util.LinkedList;
import java.util.ListIterator;

public class HistoryTracker<T extends HistoryItem> {

	private LinkedList<T> history;
	private ListIterator<T> iter;
	private int index;

	public HistoryTracker() {
		this.history = new LinkedList<>();
		this.iter = history.listIterator();
		this.index = -1;
	}

	public int getLength() {
		return history.size();
	}

	public void add(T item) {
		if (hasNext()) {
			iter.next();
			// cut off anything after the current point and start a new time line
			while (iter.hasNext()) {
				iter.next();
				iter.remove();
			}
		}
		index++;
		history.add(item);
		iter = history.listIterator(history.size() - 1);
	}

	public T goBack() {
		if (!hasPrevious()) {
			throw new IllegalStateException("Can not rewind history when already at start");
		}
		index--;
		return iter.previous();
	}

	public T peekPrevious() {
		if (!hasPrevious()) {
			return null;
		}
		T prev = iter.previous();
		iter.next();
		return prev;
	}
	
	public T peekBack(int steps) {
		int slot = index - steps + 1;
		if (slot < 0) {
			return null;
		}
		return history.get(slot);
	}

	public boolean hasNext() {
		return index < history.size() - 1;
	}

	public boolean hasPrevious() {
		return index > 0;
	}

	public boolean isEmpty() {
		return history.isEmpty();
	}

	public T goForward() {
		if (!hasNext()) {
			throw new IllegalStateException("Can not forward history when already at end");
		}
		iter.next();
		index++;
		T next = iter.next();
		iter.previous();
		return next;
	}
	
	public T peekForward() {
		if (!hasNext()) {
			return null;
		}
		iter.next();
		T next = iter.next();
		iter.previous();
		iter.previous();
		return next;
	}

	public T getCurrent() {
		T prev = iter.previous();
		iter.next();
		return prev;
	}

}
