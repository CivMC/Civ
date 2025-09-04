package vg.civcraft.mc.civmodcore.inventory.gui.components;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.gui.LClickable;

public class Scrollbar extends InventoryComponent {

    private final List<IClickable> unpaginatedContent;
    private int page;
    private int totalPages;
    private int backClickSlot;
    private int forwardClickSlot;
    private ContentAligner contentAligner;

    // Capacity (max items per page before subtracting buttons)
    private final int itemsPerPage;
    // Step (how far to advance the window each page). If < itemsPerPage => overlap.
    private int step;

    public Scrollbar(List<IClickable> content, int staticSize) {
        this(content, staticSize, staticSize);
    }

    public Scrollbar(List<IClickable> content, int staticSize, int scrollOffset) {
        this(content, staticSize, scrollOffset, ContentAligners.getLeftAligned());
    }

    public Scrollbar(List<IClickable> content, int staticSize, int itemsPerPage, ContentAligner contentAligner) {
        super(staticSize);
        this.unpaginatedContent = (content != null) ? new ArrayList<>(content) : new ArrayList<>();
        this.page = 0;
        // Clamp values to container
        int cap = Math.max(1, Math.min(itemsPerPage, getSize()));
        this.itemsPerPage = cap;
        this.step = cap; // default: paginator (no overlap)

        this.backClickSlot = 0;
        this.forwardClickSlot = staticSize - 1;
        this.contentAligner = contentAligner;

        this.totalPages = calculateTotalPages();
    }

    // Constructor with custom step, simulate scrolling.
    public Scrollbar(List<IClickable> content, int staticSize, int itemsPerPage, int step, ContentAligner aligner) {
        super(staticSize);
        this.unpaginatedContent = (content != null) ? new ArrayList<>(content) : new ArrayList<>();
        this.page = 0;

        // Clamp values to container
        this.itemsPerPage = Math.max(1, Math.min(itemsPerPage, getSize()));
        this.step = Math.max(1, step);

        this.backClickSlot = 0;
        this.forwardClickSlot = staticSize - 1;
        this.contentAligner = aligner;

        this.totalPages = calculateTotalPages();
    }

    public void setBackwardsClickSlot(int backClickSlot) {
        this.backClickSlot = backClickSlot; }

    public void setForwardClickSlot(int forwardClickSlot) {
        this.forwardClickSlot = forwardClickSlot; }

    public void addItem(IClickable toAdd) {
        this.unpaginatedContent.add(toAdd);
        this.totalPages = calculateTotalPages();
        rebuild();
    }

    public int getPage() { return page; }

    public void setPage(int page) {
        if (totalPages <= 0) { this.page = 0; return; }
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;
        this.page = page;
    }

    @Override
    protected void rebuild() {
        final int size = getSize();
        final int totalItems = unpaginatedContent.size();

        // 1) hard clear to avoid ghosts
        for (int i = 0; i < size; i++) this.content.set(i, null);

        // 2) compute start and capacity for this page
        int startIndex = startIndexForPage(page, totalItems);
        int cap = capacityForPage(page, totalItems, startIndex); // items we can place this page (after buttons)
        cap = Math.max(0, Math.min(cap, totalItems - startIndex));

        boolean showPrev = hasPrev(page);
        boolean showNext = hasNext(page, totalItems, startIndex, cap);

        // 3) place buttons first
        if (showPrev && isValidSlot(backClickSlot)) {
            this.content.set(backClickSlot, getBackwardClick());
        }
        if (showNext && isValidSlot(forwardClickSlot)) {
            this.content.set(forwardClickSlot, getForwardClick());
        }

        // 4) collect fillable slots (avoid button positions)
        List<Integer> fillable = new ArrayList<>(size);
        contentAligner.reset();
        for (int i = 0; i < size; i++) {
            int s = contentAligner.getNext();
            if (!isValidSlot(s)) continue;
            if (showPrev && s == backClickSlot) continue;
            if (showNext && s == forwardClickSlot) continue;
            fillable.add(s);
            if (fillable.size() >= cap) break;
        }

        // 5) place items
        int idx = startIndex;
        for (int s : fillable) {
            IClickable click = (idx < totalItems) ? unpaginatedContent.get(idx) : null;
            this.content.set(s, click);
            idx++;
        }
    }

    private boolean isValidSlot(int s) { return s >= 0 && s < getSize(); }

    private IClickable getBackwardClick() {
        return new LClickable(Material.ARROW, "Show previous page", p -> {
            setPage(getPage() - 1);
            update();
        });
    }
    private IClickable getForwardClick() {
        return new LClickable(Material.ARROW, "Show next page", p -> {
            setPage(getPage() + 1);
            update();
        });
    }

    // -------- Paging math --------

    private boolean hasPrev(int p) { return p > 0; }

    private boolean hasNext(int p, int totalItems, int startIndex, int capacityThisPage) {
        // If we scroll like a real scrollbox, "next" exists when there are items beyond startIndex + step.
        // This mirrors how a window advances in continuous content.
        int nextStart = startIndex + step;
        return nextStart < totalItems;
    }

    /** Start index = page * step (but clamped so we don't start past the end). */
    private int startIndexForPage(int p, int totalItems) {
        int idx = Math.max(0, p * Math.max(1, step));
        // clamp: if start is beyond last item, pull it back so we can still show a partially filled page
        if (idx >= totalItems) {
            // back up by one step to show the final window (unless list is empty)
            idx = Math.max(0, totalItems == 0 ? 0 : Math.max(0, totalItems - step));
        }
        return idx;
    }

    /** Capacity after accounting for buttons on this page. */
    private int capacityForPage(int p, int totalItems, int startIndex) {
        int base = Math.min(itemsPerPage, getSize());
        boolean prev = hasPrev(p);
        boolean next = (startIndex + step) < totalItems; // next page exists if another window fits ahead
        int buttons = (prev ? 1 : 0) + (next ? 1 : 0);
        int cap = base - buttons;
        return Math.max(0, cap);
    }

    /** Simulate page count using step-based windows. */
    private int calculateTotalPages() {
        int n = unpaginatedContent.size();
        if (n <= 0) return 1;

        int s = Math.max(1, step);
        int pages = (n + s - 1) / s; // ceil(n / step)
        return Math.max(1, pages);
    }
}
