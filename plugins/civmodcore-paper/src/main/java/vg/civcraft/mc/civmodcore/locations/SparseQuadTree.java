package vg.civcraft.mc.civmodcore.locations;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

// This isn't designed to contain absolutely HUGE boxes. When the box sizes
//  encompass the entirety of -MAX_INT to MAX_INT on both the x and y,
//  it can't handle it. That is to say, it will start splitting many levels
//  deep and the all encompassing boxes will exist in every tree at every
//  level, bringing the process to its knees. Boxes with x,y spanning a
//  million coordinates work just fine and should be sufficient.

public class SparseQuadTree<T extends QTBox> {

	public enum Quadrant {
		ROOT, NW, SW, NE, SE
	}

	public final int MAX_NODE_SIZE = 32;

	protected Integer borderSize = 0;

	protected Quadrant quadrant;

	protected Integer middleX;

	protected Integer middleZ;

	protected int size;

	protected int maxNodeSize = MAX_NODE_SIZE;

	protected Set<T> boxes;

	protected SparseQuadTree<T> nw_;

	protected SparseQuadTree<T> ne_;

	protected SparseQuadTree<T> sw_;

	protected SparseQuadTree<T> se_;

	public SparseQuadTree() {
		boxes = new TreeSet<>();
		borderSize = 0;
		quadrant = Quadrant.ROOT;
	}

	public SparseQuadTree(Integer borderSize) {
		boxes = new TreeSet<>();
		if (borderSize == null || borderSize < 0) {
			throw new IllegalArgumentException("borderSize == null || borderSize < 0");
		}
		this.borderSize = borderSize;
		this.quadrant = Quadrant.ROOT;
	}

	protected SparseQuadTree(Integer borderSize, Quadrant quadrant) {
		this.boxes = new TreeSet<>();
		this.borderSize = borderSize;
		this.quadrant = quadrant;
	}

	public void add(T box) {
		add(box, false);
	}

	protected void add(T box, boolean inSplit) {
		++size;
		if (boxes != null) {
			boxes.add(box);
			if (!inSplit) {
				split();
			}
			return;
		}
		if (box.qtXMin() - borderSize <= middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				nw_.add(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				sw_.add(box);
			}
		}
		if (box.qtXMax() + borderSize > middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				ne_.add(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				se_.add(box);
			}
		}
	}

	public String boxCoord(T box) {
		return String.format("(%d,%d %d,%d)", box.qtXMin(), box.qtZMin(), box.qtXMax(), box.qtZMax());
	}

	public Set<T> find(int x, int y) {
		return this.find(x, y, false);
	}

	public Set<T> find(int x, int y, boolean includeBorder) {
		int border = 0;
		if (includeBorder) {
			border = borderSize;
		}
		if (boxes != null) {
			Set<T> result = new TreeSet<>();
			// These two loops are the same except for the second doesn't include the
			// border adjustment for a little added performance.
			if (includeBorder) {
				for (T box : boxes) {
					if (box.qtXMin() - border <= x && box.qtXMax() + border >= x && box.qtZMin() - border <= y
							&& box.qtZMax() + border >= y) {
						result.add(box);
					}
				}
			} else {
				for (T box : boxes) {
					if (box.qtXMin() <= x && box.qtXMax() >= x && box.qtZMin() <= y && box.qtZMax() >= y) {
						result.add(box);
					}
				}
			}
			return result;
		}
		if (x <= middleX) {
			if (y <= middleZ) {
				return nw_.find(x, y, includeBorder);
			} else {
				return sw_.find(x, y, includeBorder);
			}
		}
		if (y <= middleZ) {
			return ne_.find(x, y, includeBorder);
		}
		return se_.find(x, y, includeBorder);
	}

	public int getBorderSize() {
		return borderSize;
	}

	public void remove(T box) {
		if (size <= 0) {
			size = 0;
			return;
		}
		--size;
		if (size == 0) {
			boxes = new TreeSet<>();
			nw_ = null;
			ne_ = null;
			sw_ = null;
			se_ = null;
			return;
		}
		if (boxes != null) {
			boxes.remove(box);
			return;
		}
		if (box.qtXMin() - borderSize <= middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				nw_.remove(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				sw_.remove(box);
			}
		}
		if (box.qtXMax() + borderSize > middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				ne_.remove(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				se_.remove(box);
			}
		}
	}

	protected void setMaxNodeSize(int size) {
		maxNodeSize = size;
	}

	public int size() {
		return size;
	}

	protected void split() {
		if (boxes == null || boxes.size() <= maxNodeSize) {
			return;
		}
		nw_ = new SparseQuadTree<>(borderSize, Quadrant.NW);
		ne_ = new SparseQuadTree<>(borderSize, Quadrant.NE);
		sw_ = new SparseQuadTree<>(borderSize, Quadrant.SW);
		se_ = new SparseQuadTree<>(borderSize, Quadrant.SE);
		SortedSet<Integer> xAxis = new TreeSet<>();
		SortedSet<Integer> yAxis = new TreeSet<>();
		for (QTBox box : boxes) {
			int x;
			int y;
			switch (quadrant) {
			case NW:
				x = box.qtXMin();
				y = box.qtZMin();
				break;
			case NE:
				x = box.qtXMax();
				y = box.qtZMin();
				break;
			case SW:
				x = box.qtXMin();
				y = box.qtZMax();
				break;
			case SE:
				x = box.qtXMax();
				y = box.qtZMax();
				break;
			default:
				x = box.qtXMid();
				y = box.qtZMid();
				break;
			}
			xAxis.add(x);
			yAxis.add(y);
		}
		int counter = 0;
		int ender = (xAxis.size() / 2) - 1;
		for (Integer i : xAxis) {
			if (counter >= ender) {
				middleX = i;
				break;
			}
			++counter;
		}
		counter = 0;
		ender = (yAxis.size() / 2) - 1;
		for (Integer i : yAxis) {
			if (counter >= ender) {
				middleZ = i;
				break;
			}
			++counter;
		}
		for (T box : boxes) {
			if (box.qtXMin() - borderSize <= middleX) {
				if (box.qtZMin() - borderSize <= middleZ) {
					nw_.add(box, true);
				}
				if (box.qtZMax() + borderSize > middleZ) {
					sw_.add(box, true);
				}
			}
			if (box.qtXMax() + borderSize > middleX) {
				if (box.qtZMin() - borderSize <= middleZ) {
					ne_.add(box, true);
				}
				if (box.qtZMax() + borderSize > middleZ) {
					se_.add(box, true);
				}
			}
		}
		if (nw_.size() == boxes.size() || sw_.size() == boxes.size() || ne_.size() == boxes.size()
				|| se_.size() == boxes.size()) {
			// Splitting failed as we split into an identically sized quadrent. Update
			// this nodes max size for next time and throw away the work we did.
			maxNodeSize = boxes.size() * 2;
			return;
		}
		boolean sizeAdjusted = false;
		if (nw_.size() >= maxNodeSize) {
			maxNodeSize = nw_.size() * 2;
			sizeAdjusted = true;
		}
		if (sw_.size() >= maxNodeSize) {
			maxNodeSize = sw_.size() * 2;
			sizeAdjusted = true;
		}
		if (ne_.size() >= maxNodeSize) {
			maxNodeSize = ne_.size() * 2;
			sizeAdjusted = true;
		}
		if (se_.size() >= maxNodeSize) {
			maxNodeSize = se_.size() * 2;
			sizeAdjusted = true;
		}
		if (sizeAdjusted) {
			nw_.setMaxNodeSize(maxNodeSize);
			sw_.setMaxNodeSize(maxNodeSize);
			ne_.setMaxNodeSize(maxNodeSize);
			se_.setMaxNodeSize(maxNodeSize);
		}
		boxes = null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(quadrant);
		if (boxes != null) {
			sb.append('[');
			for (T box : boxes) {
				sb.append(boxCoord(box));
			}
			sb.append(']');
			return sb.toString();
		}
		sb.append(String.format("{{%d,%d}", middleX, middleZ));
		sb.append(nw_.toString());
		sb.append(',');
		sb.append(sw_.toString());
		sb.append(',');
		sb.append(ne_.toString());
		sb.append(',');
		sb.append(se_.toString());
		sb.append('}');
		return sb.toString();
	}

}
