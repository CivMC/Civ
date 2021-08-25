package vg.civcraft.mc.civmodcore.inventory.gui.canvas;

import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.type.AbstractCivMenu;
import org.ipvp.canvas.type.CivChestMenu;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * Utility to automate creating views, which have multiple pages and automatically adjust their size
 *
 */
public class MultiPageCanvas {

	private final Player viewer;
	private final Component title;
	private final List<SlotSettings> slots;
	private final boolean adjustSize;
	private final SlotSettings[] extraMenuItems;

	private AbstractCivMenu window;
	private int currentPage;

	public MultiPageCanvas(@Nonnull final Player viewer,
						   @Nullable final Component title,
						   @Nonnull final List<SlotSettings> slots,
						   final boolean adjustSize) {
		this.viewer = Objects.requireNonNull(viewer);
		this.title = title;
		this.slots = List.copyOf(slots);
		this.adjustSize = adjustSize;
		this.extraMenuItems = new SlotSettings[7];
		this.currentPage = 0;
	}

	private void INTERNAL_constructInventory() {
		// Clamp the total page within the valid range
		if (this.currentPage < 0) {
			this.currentPage = 0;
		}
		final int totalPages = INTERNAL_calculateTotalPages();
		if (this.currentPage >= totalPages) {
			this.currentPage = totalPages - 1;
		}
		// Update the window instance if necessary
		final int totalRows = INTERNAL_calculateTotalRows();
		if (this.window == null) {
			this.window = new CivChestMenu(this.title, totalRows);
		}
		else {
			this.window.clear();
			if (this.adjustSize && totalRows != this.window.getDimensions().getRows()) {
				this.window = new CivChestMenu(this.title, totalRows);
			}
		}
		// Add all content slots
		final int contentOffset = this.currentPage * InventoryUtils.CHEST_5_ROWS;
		final int contentLength = Math.min(this.slots.size() - contentOffset, InventoryUtils.CHEST_5_ROWS);
		for (int i = 0; i < contentLength; i++) {
			this.window.getSlot(i).setSettings(this.slots.get(contentOffset + i));
		}
		final int totalSlots = this.window.getDimensions().getArea();
		// Back button
		if (this.currentPage > 0) {
			this.window.getSlot(totalSlots - 10).setSettings(SlotSettings.builder()
					.itemTemplate((ignored) -> {
						final var item = new ItemStack(Material.ARROW);
						ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
							meta.displayName(Component.text()
									.color(NamedTextColor.GOLD)
									.content("Go to previous page")
									.build());
							return true;
						});
						return item;
					})
					.clickHandler((clicker, click) -> {
						if (MultiPageCanvas.this.currentPage > 0) {
							MultiPageCanvas.this.currentPage--;
						}
						showScreen();
					})
					.build());
		}
		// Next button
		if (this.currentPage < totalPages) {
			this.window.getSlot(totalSlots - 1).setSettings(SlotSettings.builder()
					.itemTemplate((ignored) -> {
						final var item = new ItemStack(Material.ARROW);
						ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
							meta.displayName(Component.text()
									.color(NamedTextColor.GOLD)
									.content("Go to next page")
									.build());
							return true;
						});
						return item;
					})
					.clickHandler((clicker, click) -> {
						if (MultiPageCanvas.this.currentPage < totalPages) {
							MultiPageCanvas.this.currentPage++;
						}
						showScreen();
					})
					.build());
		}
		// Other menu items
		for (int i = 0; i < this.extraMenuItems.length; i++) {
			final SlotSettings slot = this.extraMenuItems[i];
			if (slot == null) {
				continue;
			}
			this.window.getSlot(totalSlots - 9 + i).setSettings(slot);
		}
	}

	protected int INTERNAL_calculateTotalPages() {
		final int size = this.slots.size();
		int pages = size / InventoryUtils.CHEST_5_ROWS;
		if ((size % InventoryUtils.CHEST_5_ROWS) > 0) {
			pages++;
		}
		return Math.max(pages, 0);
	}

	private int INTERNAL_calculateTotalRows() {
		if (!this.adjustSize) {
			return 6;
		}
		final int clicks = this.slots.size();
		if (clicks >= InventoryUtils.CHEST_5_ROWS) {
			return 6;
		}
		final int rows = (clicks / 9) + ((clicks % 9) > 0 ? 1 : 0);
		return Math.max(rows, 2); // Always have at least 2 rows
	}

	/**
	 * Shows the current page
	 */
	public void showScreen() {
		INTERNAL_constructInventory();
		this.window.open(this.viewer);
	}

	/**
	 * Allows setting a menu slot at the bottom of the GUI. Will not update the inventory, you'll need to do that
	 * manually with {@link #showScreen()}.
	 *
	 * @param slot The slot to set.
	 * @param index The index of the slot. Must be between 0 and 6 inclusively.
	 */
	public void setMenuSlot(@Nullable final SlotSettings slot, final int index) {
		if (index < 0 || index > 6) {
			throw new IllegalArgumentException("Index for MultiPageView menu must be between 0 and 6");
		}
		this.extraMenuItems[index] = slot;
	}

	/**
	 * Forcibly changes the page of this view. Will not update the inventory, you'll need to do that manually with
	 * {@link #showScreen()}.
	 *
	 * @param page The page to set.
	 */
	public void setPage(final int page) {
		this.currentPage = page;
	}

}
