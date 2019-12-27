package vg.civcraft.mc.civmodcore.locations;

import java.util.HashSet;
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
		ROOT, NORTH_WEST, SOUTH_WEST, NORTH_EAST, SOUTH_EAST
	}

	public static final int MAX_NODE_SIZE = 32;

	protected Integer borderSize = 0;

	protected Quadrant quadrant;

	protected Integer middleX;
	protected Integer middleZ;

	protected int size;

	protected int maxNodeSize = MAX_NODE_SIZE;

	protected Set<T> boxes;

	protected SparseQuadTree<T> northWest;
	protected SparseQuadTree<T> northEast;
	protected SparseQuadTree<T> southWest;
	protected SparseQuadTree<T> southEast;

	public SparseQuadTree() {
		this(0);
	}

	public SparseQuadTree(Integer borderSize) {
		boxes = new HashSet<>();
		if (borderSize == null || borderSize < 0) {
			throw new IllegalArgumentException("borderSize == null || borderSize < 0");
		}
		this.borderSize = borderSize;
		this.quadrant = Quadrant.ROOT;
	}

	protected SparseQuadTree(Integer borderSize, Quadrant quadrant) {
		this.boxes = new HashSet<>();
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
				northWest.add(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				southWest.add(box);
			}
		}
		if (box.qtXMax() + borderSize > middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				northEast.add(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				southEast.add(box);
			}
		}
	}

	public String boxCoord(T box) {
		return String.format("(%d,%d %d,%d)", box.qtXMin(), box.qtZMin(), box.qtXMax(), box.qtZMax());
	}

	public Set<T> find(int x, int z) {
		return this.find(x, z, false);
	}

	public Set<T> find(int x, int z, boolean includeBorder) {
		int border = 0;
		if (includeBorder) {
			border = borderSize;
		}
		if (boxes != null) {
			Set<T> result = new HashSet<>();
			// These two loops are the same except for the second doesn't include the
			// border adjustment for a little added performance.
			if (includeBorder) {
				for (T box : boxes) {
					if (box.qtXMin() - border <= x && box.qtXMax() + border >= x && box.qtZMin() - border <= z
							&& box.qtZMax() + border >= z) {
						result.add(box);
					}
				}
			} else {
				for (T box : boxes) {
					if (box.qtXMin() <= x && box.qtXMax() >= x && box.qtZMin() <= z && box.qtZMax() >= z) {
						result.add(box);
					}
				}
			}
			return result;
		}
		if (x <= middleX) {
			if (z <= middleZ) {
				return northWest.find(x, z, includeBorder);
			} else {
				return southWest.find(x, z, includeBorder);
			}
		}
		if (z <= middleZ) {
			return northEast.find(x, z, includeBorder);
		}
		return southEast.find(x, z, includeBorder);
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
			boxes = new HashSet<>();
			northWest = null;
			northEast = null;
			southWest = null;
			southEast = null;
			return;
		}
		if (boxes != null) {
			boxes.remove(box);
			return;
		}
		if (box.qtXMin() - borderSize <= middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				northWest.remove(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				southWest.remove(box);
			}
		}
		if (box.qtXMax() + borderSize > middleX) {
			if (box.qtZMin() - borderSize <= middleZ) {
				northEast.remove(box);
			}
			if (box.qtZMax() + borderSize > middleZ) {
				southEast.remove(box);
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
		northWest = new SparseQuadTree<>(borderSize, Quadrant.NORTH_WEST);
		northEast = new SparseQuadTree<>(borderSize, Quadrant.NORTH_EAST);
		southWest = new SparseQuadTree<>(borderSize, Quadrant.SOUTH_WEST);
		southEast = new SparseQuadTree<>(borderSize, Quadrant.SOUTH_EAST);
		SortedSet<Integer> xAxis = new TreeSet<>();
		SortedSet<Integer> zAxis = new TreeSet<>();
		for (QTBox box : boxes) {
			int x;
			int z;
			switch (quadrant) {
			case NORTH_WEST:
				x = box.qtXMin();
				z = box.qtZMin();
				break;
			case NORTH_EAST:
				x = box.qtXMax();
				z = box.qtZMin();
				break;
			case SOUTH_WEST:
				x = box.qtXMin();
				z = box.qtZMax();
				break;
			case SOUTH_EAST:
				x = box.qtXMax();
				z = box.qtZMax();
				break;
			default:
				x = box.qtXMid();
				z = box.qtZMid();
				break;
			}
			xAxis.add(x);
			zAxis.add(z);
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
		ender = (zAxis.size() / 2) - 1;
		for (Integer i : zAxis) {
			if (counter >= ender) {
				middleZ = i;
				break;
			}
			++counter;
		}
		for (T box : boxes) {
			if (box.qtXMin() - borderSize <= middleX) {
				if (box.qtZMin() - borderSize <= middleZ) {
					northWest.add(box, true);
				}
				if (box.qtZMax() + borderSize > middleZ) {
					southWest.add(box, true);
				}
			}
			if (box.qtXMax() + borderSize > middleX) {
				if (box.qtZMin() - borderSize <= middleZ) {
					northEast.add(box, true);
				}
				if (box.qtZMax() + borderSize > middleZ) {
					southEast.add(box, true);
				}
			}
		}
		if (northWest.size() == boxes.size() || southWest.size() == boxes.size() || northEast.size() == boxes.size()
				|| southEast.size() == boxes.size()) {
			// Splitting failed as we split into an identically sized quadrent. Update
			// this nodes max size for next time and throw away the work we did.
			maxNodeSize = boxes.size() * 2;
			return;
		}
		boolean sizeAdjusted = false;
		if (northWest.size() >= maxNodeSize) {
			maxNodeSize = northWest.size() * 2;
			sizeAdjusted = true;
		}
		if (southWest.size() >= maxNodeSize) {
			maxNodeSize = southWest.size() * 2;
			sizeAdjusted = true;
		}
		if (northEast.size() >= maxNodeSize) {
			maxNodeSize = northEast.size() * 2;
			sizeAdjusted = true;
		}
		if (southEast.size() >= maxNodeSize) {
			maxNodeSize = southEast.size() * 2;
			sizeAdjusted = true;
		}
		if (sizeAdjusted) {
			northWest.setMaxNodeSize(maxNodeSize);
			southWest.setMaxNodeSize(maxNodeSize);
			northEast.setMaxNodeSize(maxNodeSize);
			southEast.setMaxNodeSize(maxNodeSize);
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
		sb.append(northWest.toString());
		sb.append(',');
		sb.append(southWest.toString());
		sb.append(',');
		sb.append(northEast.toString());
		sb.append(',');
		sb.append(southEast.toString());
		sb.append('}');
		return sb.toString();
	}

}
