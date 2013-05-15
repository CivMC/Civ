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
  public SparseQuadTree() {
    boxes_ = new TreeSet<QTBox>();
  }

  public void add(QTBox box) {
    add(box, false);
  }

  private void add(QTBox box, boolean inSplit) {
    ++size_;
    if (boxes_ != null) {
      boxes_.add(box);
      if (!inSplit) {
        split();
      }
      return;
    }
    if (box.qtX1() <= midX_) {
      if (box.qtY1() <= midY_) {
        nw_.add(box);
      }
      if (box.qtY2() > midY_) {
        sw_.add(box);
      }
    }
    if (box.qtX2() > midX_) {
      if (box.qtY1() <= midY_) {
        ne_.add(box);
      }
      if (box.qtY2() > midY_) {
        se_.add(box);
      }
    }
  }

  public void remove(QTBox box) {
    --size_;
    if (boxes_ != null) {
      boxes_.remove(box);
      return;
    }
    if (box.qtX1() <= midX_) {
      if (box.qtY1() <= midY_) {
        nw_.remove(box);
      }
      if (box.qtY2() > midY_) {
        sw_.remove(box);
      }
    }
    if (box.qtX2() > midX_) {
      if (box.qtY1() <= midY_) {
        ne_.remove(box);
      }
      if (box.qtY2() > midY_) {
        se_.remove(box);
      }
    }
  }

  public int size() {
    return size_;
  }

  public Set<QTBox> find(int x, int y) {
    if (boxes_ != null) {
      Set<QTBox> result = new TreeSet<QTBox>();
      for (QTBox box : boxes_) {
        if (box.qtX1() <= x && box.qtX2() >= x
            && box.qtY1() <= y && box.qtY2() >= y) {
          result.add(box);
        }
      }
      return result;
    }
    if (x <= midX_) {
      if (y <= midY_) {
        return nw_.find(x, y);
      } else {
        return sw_.find(x, y);
      }
    }
    if (y <= midY_) {
      return ne_.find(x, y);
    }
    return se_.find(x, y);
  }

  private void split() {
    if (boxes_ == null || boxes_.size() <= maxNodeSize_) {
      return;
    }
    nw_ = new SparseQuadTree();
    ne_ = new SparseQuadTree();
    sw_ = new SparseQuadTree();
    se_ = new SparseQuadTree();
    SortedSet<Integer> xAxis = new TreeSet<Integer>();
    SortedSet<Integer> yAxis = new TreeSet<Integer>();
    for (QTBox box : boxes_) {
      xAxis.add(box.qtX2());
      yAxis.add(box.qtY2());
    }
    int counter = 0;
    int ender = (xAxis.size() / 2) - 1;
    for (Integer i : xAxis) {
      if (counter >= ender) {
        midX_ = i + 1;
        break;
      }
      ++counter;
    }
    counter = 0;
    for (Integer i : yAxis) {
      if (counter >= ender) {
        midY_ = i + 1;
        break;
      }
      ++counter;
    }
    for (QTBox box : boxes_) {
      if (box.qtX1() <= midX_) {
        if (box.qtY1() <= midY_) {
          nw_.add(box, true);
        }
        if (box.qtY2() > midY_) {
          sw_.add(box, true);
        }
      }
      if (box.qtX2() > midX_) {
        if (box.qtY1() <= midY_) {
          ne_.add(box, true);
        }
        if (box.qtY2() > midY_) {
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

  private void setMaxNodeSize(int size) {
    maxNodeSize_ = size;
  }

  public String boxCoord(QTBox box) {
    return String.format("(%d,%dx%d,%d)", box.qtX1(), box.qtY1(), box.qtX2(), box.qtY2());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
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

  private Integer midX_ = null;
  private Integer midY_ = null;
  private int size_;
  private int maxNodeSize_ = 32;
  private Set<QTBox> boxes_;
  private SparseQuadTree nw_;
  private SparseQuadTree ne_;
  private SparseQuadTree sw_;
  private SparseQuadTree se_;
}
