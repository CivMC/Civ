package vg.civcraft.mc.civmodcore.world.locations;

import com.github.davidmoten.rtree2.geometry.Rectangle;
import com.github.davidmoten.rtree2.geometry.internal.RectangleDouble;

public interface QTBox {

    int qtXMin();

    int qtXMid();

    int qtXMax();

    int qtZMin();

    int qtZMid();

    int qtZMax();

    default Rectangle asRectangle() {
        return RectangleDouble.create(qtXMin(), qtZMin(), qtXMax(), qtZMax());
    }
}
