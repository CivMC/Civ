package vg.civcraft.mc.civmodcore.util.progress;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ProgressTracker<T extends ProgressTrackable> {

	private TreeSet<T> queueItems;

	public ProgressTracker() {
		this.queueItems = new TreeSet<>((a, b) -> {
			int timeDiff = Long.compare(a.getNextUpdate(), b.getNextUpdate());
			if (timeDiff == 0) {
				return a.compareTo(b);
			}
			return timeDiff;
		});
	}

	public void addItem(T trackable) {
		if (trackable.getNextUpdate() == Long.MAX_VALUE) {
			queueItems.add(trackable);
		}
	}

	public long processItems() {
		long nextOne = Long.MAX_VALUE;
		long time = System.currentTimeMillis();
		Iterator<T> iter = queueItems.iterator();
		List<T> toReAdd = new ArrayList<>();
		while (iter.hasNext()) {
			T item = iter.next();
			if (item.getNextUpdate() > time) {
				nextOne = Math.min(nextOne, item.getNextUpdate());
				break;
			}
			iter.remove();
			item.updateState();
			if (item.getNextUpdate() > 0) {
				toReAdd.add(item);
			}
		}
		for (T item : toReAdd) {
			nextOne = Math.min(nextOne, item.getNextUpdate());
			addItem(item);
		}
		return nextOne;
	}

	public void removeItem(T trackable) {
		queueItems.remove(trackable);
	}

	public void updateItem(T trackable, long nextTime) {
		queueItems.remove(trackable);
		trackable.updateInternalProgressTime(nextTime);
		addItem(trackable);
	}

}
