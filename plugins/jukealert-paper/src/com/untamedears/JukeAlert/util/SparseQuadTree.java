package com.untamedears.JukeAlert.util;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.untamedears.JukeAlert.util.QTBox;

// This isn't designed to contain absolutely HUGE boxes. When the box sizes
//  encompass the entirety of -MAX_INT to MAX_INT on both the x and y,
//  it can't handle it. That is to say, it will start splitting many levels
//  deep and the all encompassing boxes will exist in every tree at every
//  level, bringing the process to its knees. Boxes with x,y spanning a
//  million coordinates work just fine and should be sufficient.

public class SparseQuadTree {
  public final int MAX_NODE_SIZE = 32;

  public enum Quadrant {
    Root,
    NW,
    SW,
    NE,
    SE
  }

  public SparseQuadTree() {
    boxes_ = new TreeSet<QTBox>();
    borderSize_ = 0;
    quadrant_ = Quadrant.Root;
  }

  public SparseQuadTree(Integer borderSize) {
    boxes_ = new TreeSet<QTBox>();
    if (borderSize == null || borderSize < 0) {
        throw new IllegalArgumentException(
            "borderSize == null || borderSize < 0");
    }
    borderSize_ = borderSize;
    quadrant_ = Quadrant.Root;
  }

  protected SparseQuadTree(Integer borderSize, Quadrant quadrant) {
    boxes_ = new TreeSet<QTBox>();
    borderSize_ = borderSize;
    quadrant_ = quadrant;
  }

  public void add(QTBox box) {
    add(box, false);
  }

  protected void add(QTBox box, boolean inSplit) {
    ++size_;
    if (boxes_ != null) {
      boxes_.add(box);
      if (!inSplit) {
        split();
      }
      return;
    }
    if (box.qtXMin() - borderSize_ <= midX_) {
      if (box.qtYMin() - borderSize_ <= midY_) {
        nw_.add(box);
      }
      if (box.qtYMax() + borderSize_ > midY_) {
        sw_.add(box);
      }
    }
    if (box.qtXMax() + borderSize_ > midX_) {
      if (box.qtYMin() - borderSize_ <= midY_) {
        ne_.add(box);
      }
      if (box.qtYMax() + borderSize_ > midY_) {
        se_.add(box);
      }
    }
  }

  public void remove(QTBox box) {
    if (size_ <= 0) {
      size_ = 0;
      return;
    }
    --size_;
    if (size_ == 0) {
      boxes_ = new TreeSet<QTBox>();
      nw_ = null;
      ne_ = null;
      sw_ = null;
      se_ = null;
      return;
    }
    if (boxes_ != null) {
      boxes_.remove(box);
      return;
    }
    if (box.qtXMin() - borderSize_ <= midX_) {
      if (box.qtYMin() - borderSize_ <= midY_) {
        nw_.remove(box);
      }
      if (box.qtYMax() + borderSize_ > midY_) {
        sw_.remove(box);
      }
    }
    if (box.qtXMax() + borderSize_ > midX_) {
      if (box.qtYMin() - borderSize_ <= midY_) {
        ne_.remove(box);
      }
      if (box.qtYMax() + borderSize_ > midY_) {
        se_.remove(box);
      }
    }
  }

  public int size() {
    return size_;
  }

  public Set<QTBox> find(int x, int y) {
      return this.find(x, y, false);
  }

  public Set<QTBox> find(int x, int y, boolean includeBorder) {
    int border = 0;
    if (includeBorder) {
      border = borderSize_;
    }
    if (boxes_ != null) {
      Set<QTBox> result = new TreeSet<QTBox>();
      // These two loops are the same except for the second doesn't include the
      //  border adjustment for a little added performance.
      if (includeBorder) {
        for (QTBox box : boxes_) {
          if (box.qtXMin() - border <= x && box.qtXMax() + border >= x
              && box.qtYMin() - border <= y && box.qtYMax() + border >= y) {
            result.add(box);
          }
        }
      } else {
        for (QTBox box : boxes_) {
          if (box.qtXMin() <= x && box.qtXMax() >= x
              && box.qtYMin() <= y && box.qtYMax() >= y) {
            result.add(box);
          }
        }
      }
      return result;
    }
    if (x <= midX_) {
      if (y <= midY_) {
        return nw_.find(x, y, includeBorder);
      } else {
        return sw_.find(x, y, includeBorder);
      }
    }
    if (y <= midY_) {
      return ne_.find(x, y, includeBorder);
    }
    return se_.find(x, y, includeBorder);
  }

  protected void split() {
    if (boxes_ == null || boxes_.size() <= maxNodeSize_) {
      return;
    }
    nw_ = new SparseQuadTree(borderSize_, Quadrant.NW);
    ne_ = new SparseQuadTree(borderSize_, Quadrant.NE);
    sw_ = new SparseQuadTree(borderSize_, Quadrant.SW);
    se_ = new SparseQuadTree(borderSize_, Quadrant.SE);
    SortedSet<Integer> xAxis = new TreeSet<Integer>();
    SortedSet<Integer> yAxis = new TreeSet<Integer>();
    for (QTBox box : boxes_) {
      int x;
      int y;
      switch (quadrant_) {
        case NW:
          x = box.qtXMin();
          y = box.qtYMin();
          break;
        case NE:
          x = box.qtXMax();
          y = box.qtYMin();
          break;
        case SW:
          x = box.qtXMin();
          y = box.qtYMax();
          break;
        case SE:
          x = box.qtXMax();
          y = box.qtYMax();
          break;
        default:
          x = box.qtXMid();
          y = box.qtYMid();
          break;
      }
      xAxis.add(x);
      yAxis.add(y);
    }
    int counter = 0;
    int ender = (xAxis.size() / 2) - 1;
    for (Integer i : xAxis) {
      if (counter >= ender) {
        midX_ = i;
        break;
      }
      ++counter;
    }
    counter = 0;
    ender = (yAxis.size() / 2) - 1;
    for (Integer i : yAxis) {
      if (counter >= ender) {
        midY_ = i;
        break;
      }
      ++counter;
    }
    for (QTBox box : boxes_) {
      if (box.qtXMin() - borderSize_ <= midX_) {
        if (box.qtYMin() - borderSize_ <= midY_) {
          nw_.add(box, true);
        }
        if (box.qtYMax() + borderSize_ > midY_) {
          sw_.add(box, true);
        }
      }
      if (box.qtXMax() + borderSize_ > midX_) {
        if (box.qtYMin() - borderSize_ <= midY_) {
          ne_.add(box, true);
        }
        if (box.qtYMax() + borderSize_ > midY_) {
          se_.add(box, true);
        }
      }
    }
    if (nw_.size() == boxes_.size()
        || sw_.size() == boxes_.size()
        || ne_.size() == boxes_.size()
        || se_.size() == boxes_.size()) {
      // Splitting failed as we split into an identically sized quadrent. Update
      //  this nodes max size for next time and throw away the work we did.
      maxNodeSize_ = boxes_.size() * 2;
      return;
    }
    boolean sizeAdjusted = false;
    if (nw_.size() >= maxNodeSize_) {
      maxNodeSize_ = nw_.size() * 2;
      sizeAdjusted = true;
    }
    if (sw_.size() >= maxNodeSize_) {
      maxNodeSize_ = sw_.size() * 2;
      sizeAdjusted = true;
    }
    if (ne_.size() >= maxNodeSize_) {
      maxNodeSize_ = ne_.size() * 2;
      sizeAdjusted = true;
    }
    if (se_.size() >= maxNodeSize_) {
      maxNodeSize_ = se_.size() * 2;
      sizeAdjusted = true;
    }
    if (sizeAdjusted) {
      nw_.setMaxNodeSize(maxNodeSize_);
      sw_.setMaxNodeSize(maxNodeSize_);
      ne_.setMaxNodeSize(maxNodeSize_);
      se_.setMaxNodeSize(maxNodeSize_);
    }
    boxes_ = null;
  }

  public int getBorderSize() {
    return borderSize_;
  }

  public String boxCoord(QTBox box) {
    return String.format("(%d,%d %d,%d)",
        box.qtXMin(), box.qtYMin(), box.qtXMax(), box.qtYMax());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(quadrant_);
    if (boxes_ != null) {
      sb.append('[');
      for (QTBox box : boxes_) {
        sb.append(boxCoord(box));
      }
      sb.append(']');
      return sb.toString();
    }
    sb.append(String.format("{{%d,%d}", midX_, midY_));
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

  protected void setMaxNodeSize(int size) {
    maxNodeSize_ = size;
  }

  protected Integer borderSize_ = 0;
  protected Quadrant quadrant_;
  protected Integer midX_ = null;
  protected Integer midY_ = null;
  protected int size_;
  protected int maxNodeSize_ = MAX_NODE_SIZE;
  protected Set<QTBox> boxes_;
  protected SparseQuadTree nw_;
  protected SparseQuadTree ne_;
  protected SparseQuadTree sw_;
  protected SparseQuadTree se_;
}
