package vg.civcraft.mc.civmodcore.inventory.gui.canvas;

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.entity.Player;
import org.ipvp.canvas.slot.SlotSettings;
import org.ipvp.canvas.type.AbstractCivMenu;

/**
 * Better version of {@link org.ipvp.canvas.paginate.PaginatedMenuBuilder} as it allows you to apply pagination to an
 * already existing menu, rather than using a builder to spawn a bunch of new menu instances that'll just be used by a
 * single player before promptly discarded.
 */
public final class MenuPagination {

	private final AbstractCivMenu menu;
	private final Map<UUID, INTERNAL_PlayerPager> playerPages;
	private final List<SlotSettings> menuBar;
	private final ElementSupplier supplier;
	private final int buttonSlots;

	private MenuPagination(final AbstractCivMenu menu,
						   final ElementSupplier supplier,
						   final List<SlotSettings> menuBar) {
		this.menu = Objects.requireNonNull(menu, "Menu cannot be null");
		final int rows = menu.getDimensions().getRows();
		if (rows < 2 || rows > 5) {
			throw new IllegalArgumentException("Row count must be between 2 and 5 inclusively");
		}
		this.playerPages = new Object2ObjectAVLTreeMap<>();
		this.menuBar = List.copyOf(menuBar);
		if (menuBar.size() > 9) {
			throw new IllegalArgumentException("Bar cannot contain more than 9 buttons");
		}
		this.supplier = Objects.requireNonNull(supplier, "Element supplier cannot be null");
		this.buttonSlots = (rows * 9) - 9;

		MenuUtils.overrideOpenHandler(menu, (viewer, opened) -> {
			final var pager = INTERNAL_getPlayerPager(viewer);
			pager.pageNumber = 0;
			pager.elements = this.supplier.generate(viewer, opened);
			INTERNAL_applyPlayerPage(viewer, opened, INTERNAL_getPlayerPager(viewer));
		}, true);

		MenuUtils.overrideUpdateHandler(menu, (viewer, updated) -> {
			INTERNAL_applyPlayerPage(viewer, updated, INTERNAL_getPlayerPager(viewer));
		}, true);

		menu.setCallCloseHandlerRegardless(true);
		MenuUtils.overrideCloseHandler(menu, (viewer, ignored) -> {
			this.playerPages.remove(viewer.getUniqueId());
		}, true);

		for (int i = 0; i < this.buttonSlots; i++) {
			menu.getSlot(i).setClickHandler((viewer, click) -> {
				final var pager = INTERNAL_getPlayerPager(viewer);
				final int buttonIndex = INTERNAL_calculateElementIndex(pager, click.getClickedSlot().getIndex());
				if (buttonIndex >= pager.elements.size()) {
					return;
				}
				final var handler = pager.elements.get(buttonIndex).getClickHandler();
				if (handler == null) {
					return;
				}
				handler.click(viewer, click);
			});
		}

		for (int i = 0; i < 9; i++) {
			final var slotIndex = INTERNAL_calculateBarIndex(i);
			if (i >= this.menuBar.size()) {
				menu.getSlot(slotIndex).setSettings(MenuUtils.createEmptySlot());
				break;
			}
			final var barElement = this.menuBar.get(i);
			if (barElement == null) {
				menu.getSlot(slotIndex).setSettings(MenuUtils.createEmptySlot());
				continue;
			}
			menu.getSlot(slotIndex).setSettings(barElement);
		}
	}

	/**
	 * @param viewer The viewer to send to the next page.
	 */
	public void nextPage(final Player viewer) {
		final var pager = INTERNAL_getPlayerPager(viewer);
		pager.pageNumber++;
		this.menu.update(viewer);
	}

	/**
	 * @param viewer The viewer to send to the previous page.
	 */
	public void previousPage(final Player viewer) {
		final var pager = INTERNAL_getPlayerPager(viewer);
		pager.pageNumber--;
		this.menu.update(viewer);
	}

	/**
	 * @param viewer The viewer to check if they have a next page.
	 * @return Returns whether the viewer has a next page.
	 */
	public boolean hasNextPage(final Player viewer) {
		final var pager = INTERNAL_getPlayerPager(viewer);
		final int elementsSize = CollectionUtils.size(pager.elements);
		final int totalPages = elementsSize / this.buttonSlots;
		if (totalPages == 0) {
			return false;
		}
		return pager.pageNumber >= totalPages;
	}

	/**
	 * @param viewer The viewer to check if they have a previous page.
	 * @return Returns whether the viewer has a previous page.
	 */
	public boolean hasPreviousPage(final Player viewer) {
		final var pager = INTERNAL_getPlayerPager(viewer);
		final int elementsSize = CollectionUtils.size(pager.elements);
		final int totalPages = elementsSize / this.buttonSlots;
		if (totalPages == 0) {
			return false;
		}
		return pager.pageNumber < totalPages;
	}

	protected void INTERNAL_applyPlayerPage(final Player viewer,
											final AbstractCivMenu menu,
											final INTERNAL_PlayerPager pager) {
		pager.pageNumber = Math.max(pager.pageNumber, 0);
		pager.pageNumber = Math.min(pager.pageNumber, pager.elements.size() / this.buttonSlots);
		for (int i = 0; i < this.buttonSlots; i++) {
			final int buttonIndex = INTERNAL_calculateElementIndex(pager, i);
			if (buttonIndex >= pager.elements.size()) {
				menu.getSlot(i).setRawItem(viewer, null);
				continue;
			}
			final var template = pager.elements.get(buttonIndex).getItemTemplate();
			if (template == null) {
				menu.getSlot(i).setRawItem(viewer, null);
				continue;
			}
			menu.getSlot(i).setRawItem(viewer, template.getItem(viewer));
		}
	}

	protected INTERNAL_PlayerPager INTERNAL_getPlayerPager(final Player viewer) {
		return this.playerPages.computeIfAbsent(viewer.getUniqueId(), (uuid) -> new INTERNAL_PlayerPager());
	}

	protected int INTERNAL_calculateElementIndex(final INTERNAL_PlayerPager pager, final int slot) {
		return (pager.pageNumber * this.buttonSlots) + slot;
	}

	protected int INTERNAL_calculateBarIndex(final int slot) {
		return this.buttonSlots + slot;
	}

	protected static class INTERNAL_PlayerPager {
		private int pageNumber;
		private List<SlotSettings> elements;
	}

	@FunctionalInterface
	public interface ElementSupplier {
		List<SlotSettings> generate(Player viewer, AbstractCivMenu menu);
	}

	/**
	 * @return Returns a new pagination builder.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private AbstractCivMenu menu;
		private ElementSupplier supplier;
		private final SlotSettings[] menuBar;

		Builder() {
			this.menuBar = new SlotSettings[] {
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot(),
					MenuUtils.createEmptySlot()
			};
		}

		/**
		 * @param menu The menu to paginate.
		 * @return Returns this builder.
		 */
		public Builder setMenu(final AbstractCivMenu menu) {
			this.menu = menu;
			return this;
		}

		/**
		 * @param supplier Supplier method to return all the elements to be paginated.
		 * @return Returns this builder.
		 */
		public Builder setElementSupplier(final ElementSupplier supplier) {
			this.supplier = supplier;
			return this;
		}

		/**
		 * @param index The index of the menu bar element from 0-8 inclusively.
		 * @param element The menu bar element to add.
		 * @return Returns this builder.
		 */
		public Builder setMenuBarElement(final int index, final SlotSettings element) {
			this.menuBar[index] = element == null ? MenuUtils.createEmptySlot() : element;
			return this;
		}

		/**
		 * @return Returns a constructed menu pagination based on this builder.
		 */
		public MenuPagination build() {
			return new MenuPagination(this.menu, this.supplier, Arrays.asList(this.menuBar));
		}

	}

}
