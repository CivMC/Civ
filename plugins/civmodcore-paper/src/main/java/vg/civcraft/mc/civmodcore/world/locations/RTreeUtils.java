package vg.civcraft.mc.civmodcore.world.locations;

import com.github.davidmoten.rtree2.Entry;
import com.github.davidmoten.rtree2.Leaf;
import com.github.davidmoten.rtree2.Node;
import com.github.davidmoten.rtree2.NonLeaf;
import com.github.davidmoten.rtree2.RTree;
import com.github.davidmoten.rtree2.geometry.Rectangle;
import java.util.Collection;
import java.util.function.Predicate;
import org.jetbrains.annotations.NotNull;

public final class RTreeUtils {

    private RTreeUtils() {
    }

    /**
     * Appends matching values directly to the supplied collection, without allocating
     * the R-tree's lazy search iterator. Rectangle edges are inclusive. The filter
     * is called for each matching entry; the collection controls deduplication.
     */
    public static <T> void searchPoint(final @NotNull RTree<T, Rectangle> tree,
                                      final double x, final double y,
                                      final @NotNull Predicate<? super T> filter,
                                      final @NotNull Collection<? super T> result) {
        final Node<T, Rectangle> root = tree.root().orElse(null);
        if (root != null) {
            searchPoint(root, x, y, filter, result);
        }
    }

    private static <T> void searchPoint(final @NotNull Node<T, Rectangle> node,
                                       final double x, final double y,
                                       final @NotNull Predicate<? super T> filter,
                                       final @NotNull Collection<? super T> result) {
        if (!node.geometry().mbr().contains(x, y)) {
            return;
        }
        if (node instanceof NonLeaf<T, Rectangle> branch) {
            for (int i = 0; i < branch.count(); i++) {
                searchPoint(branch.child(i), x, y, filter, result);
            }
        } else {
            final Leaf<T, Rectangle> leaf = (Leaf<T, Rectangle>) node;
            for (int i = 0; i < leaf.count(); i++) {
                final Entry<T, Rectangle> entry = leaf.entry(i);
                if (entry.geometry().contains(x, y) && filter.test(entry.value())) {
                    result.add(entry.value());
                }
            }
        }
    }
}
