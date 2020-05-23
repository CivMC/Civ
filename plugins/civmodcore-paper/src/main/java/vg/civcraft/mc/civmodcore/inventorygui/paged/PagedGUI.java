package vg.civcraft.mc.civmodcore.inventorygui.paged;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.util.Iteration;

/**
 * Class that represents a proverbial slideshow of pages that can be switched to efficiently.
 *
 * You could also use {@link vg.civcraft.mc.civmodcore.inventorygui.MultiPageView}
 */
public final class PagedGUI {

	private final int slots;

	private final UUID uuid;

	private final ClickableInventory inventory;

	private final Stack<Page> history;

	private Page currentPage;

	/**
	 * Creates a paged GUI based on an amount of slots and a name.
	 *
	 * @param slots The amount of slots, which must pass a {@link InventoryAPI#isValidChestSize(int)} test.
	 * @param name The name for the GUI, which should be relevant across each page as it cannot be changed.
	 */
	public PagedGUI(int slots, String name) {
		Preconditions.checkArgument(InventoryAPI.isValidChestSize(slots));
		this.slots = slots;
		this.uuid = UUID.randomUUID();
		this.inventory = new LegacyInventory(slots, name);
		this.history = new Stack<>();
		this.currentPage = null;
	}

	/**
	 * Resets the paged GUI if there's one or fewer viewers.
	 *
	 * @return Returns true if the paged GUI was reset.
	 */
	public boolean reset() {
		if (InventoryAPI.hasOtherViewers(this.inventory.getInventory())) {
			return false;
		}
		InventoryAPI.clearInventory(this.inventory.getInventory());
		this.history.clear();
		this.currentPage = null;
		return true;
	}

	/**
	 * <p>Creates a new page to be used for this paged GUI.</p>
	 *
	 * <p>Note: The returned page will be forever bound to this paged GUI, do not try to use it for another.</p>
	 *
	 * @return Returns a new page for this GUI.
	 */
	public Page createPage() {
		return new Page();
	}

	/**
	 * Presents this paged GUI to a player.
	 *
	 * @param player The player to present this paged GUI to.
	 * @return Returns true if the player was successfully presented with this paged GUI.
	 */
	public boolean showGUI(Player player) {
		Preconditions.checkArgument(player != null);
		InventoryView view = player.openInventory(getInventory());
		if (view == null) {
			return false;
		}
		PagedGUIManager.GUIs.put(getInventory(), this);
		return true;
	}

	/**
	 * <p>Handler for when this paged GUI has been clicked.</p>
	 *
	 * <p>Note: Only {@link PagedGUIManager#onInventoryClick(InventoryClickEvent)} should call this method.</p>
	 *
	 * @param slot The slot of the button that has been clicked.
	 * @param clicker The player who clicked the button.
	 */
	public void clicked(int slot, Player clicker, ClickType clickType) {
		if (this.currentPage == null) {
			return;
		}
		IClickable button = this.currentPage.getButton(slot);
		if (button == null) {
			return;
		}
		button.handleClick(clicker, clickType);
	}

	/**
	 * @return Returns how many slots this GUI has.
	 */
	public int getSlotCount() {
		return this.slots;
	}

	/**
	 * @return Returns the underlying inventory storing the items representing buttons.
	 */
	public Inventory getInventory() {
		return this.inventory.getInventory();
	}

	/**
	 * Clears this GUI's page lineage, making the current page the root page.
	 */
	public void clearPageLineage() {
		this.history.clear();
	}

	/**
	 * Gets the current page.
	 *
	 * @return Returns the current page, which may be null.
	 */
	public Page getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page. Will add the previous current page to the lineage.
	 *
	 * @param page The page to set, which cannot be null.
	 */
	public void setCurrentPage(Page page) {
		setCurrentPage(page, true);
	}

	/**
	 * Sets the current page.
	 *
	 * @param page The page to set, which cannot be null.
	 * @param addPreviousToHistory If true will add the previous current page to the lineage.
	 */
	public void setCurrentPage(Page page, boolean addPreviousToHistory) {
		Preconditions.checkArgument(page != null && page.getGUI() == this);
		Page previousPage = this.currentPage;
		if (previousPage == page) {
			return;
		}
		this.currentPage = page;
		if (previousPage != null) {
			if (addPreviousToHistory) {
				this.history.push(previousPage);
			}
			previousPage.hidden();
		}
		page.showGUI();
		updateGUI();
	}

	/**
	 * Goes to the previous page within the lineage. If no lineage can be found then the GUI is emptied.
	 */
	public void goToPreviousPage() {
		if (!this.history.isEmpty()) {
			Page page = this.history.pop();
			if (page != null) {
				setCurrentPage(page, false);
				return;
			}
		}
		this.currentPage = null;
		InventoryAPI.clearInventory(this.inventory.getInventory());
		updateGUI();
	}

	private void updateGUI() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(CivModCorePlugin.getInstance(), this.inventory::updateInventory);
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PagedGUI)) {
			return false;
		}
		if (hashCode() != other.hashCode()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.uuid, this.inventory);
	}

	/**
	 * Class that represents a page of a gui, a proverbial slide of a slideshow.
	 */
	public final class Page {

		private final IClickable[] items;

		private final Set<BukkitTask> tasks;

		private Page() {
			this.items = new IClickable[slots];
			this.tasks = new HashSet<>();
		}

		private void showGUI() {
			for (int i = 0; i < slots; i++) {
				IClickable clickable = this.items[i];
				if (clickable == null) {
					inventory.setItem(null, i);
					continue;
				}
				ItemStack button = clickable.getItemStack();
				if (!ItemAPI.isValidItem(button)) {
					inventory.setItem(null, i);
					continue;
				}
				inventory.setItem(button, i);
				clickable.addedToInventory(inventory, i);
			}
		}

		private void hidden() {
			Iteration.iterateThenClear(this.tasks, BukkitTask::cancel);
		}

		/**
		 * Resets this page to if it had just been created.
		 */
		public void reset() {
			Arrays.fill(this.items, null);
			hidden();
		}

		/**
		 * Gets the particular GUI this page is bound to. Do not attempt to use this page for another GUI, it will fail.
		 *
		 * @return Returns the GUI this page is bound to.
		 */
		public PagedGUI getGUI() {
			return PagedGUI.this;
		}

		/**
		 * Checks whether this page is currently being displayed.
		 *
		 * @return Returns true if this page is currently being displayed.
		 */
		public boolean isCurrentPage() {
			return currentPage == this;
		}

		/**
		 * Gets a button (a clickable) for a particular index.
		 *
		 * @param index The index to get the button for.
		 * @return Returns the button at the given index, or null.
		 */
		public IClickable getButton(int index) {
			return this.items[index];
		}

		/**
		 * Sets a button (a clickable) to a particular index.
		 *
		 * @param index The index to save the button to.
		 * @param button The button to save.
		 */
		public void setButton(int index, IClickable button) {
			this.items[index] = button;
		}

		/**
		 * Adds a task to this page. This feature pretty much exists to support
		 * {@link vg.civcraft.mc.civmodcore.inventorygui.AnimatedClickable AnimatedClickable}.
		 *
		 * @param task The task to add.
		 */
		public void addTask(BukkitTask task) {
			Preconditions.checkArgument(task != null);
			Preconditions.checkArgument(!task.isCancelled());
			Preconditions.checkArgument(task.isSync());
			this.tasks.add(task);
		}

		/**
		 * Removes a task from the page and will be cancelled in the process.
		 *
		 * @param task The task to remove and cancel.
		 */
		public void removeTask(BukkitTask task) {
			Preconditions.checkArgument(task != null);
			task.cancel();
			this.tasks.remove(task);
		}

	}

	/**
	 * This class is just a ClickableInventory to
	 */
	public class LegacyInventory extends ClickableInventory {

		public LegacyInventory(int size, String name) {
			super(size, name);
		}

		@Override
		public void registerTask(BukkitTask task) {
			Preconditions.checkArgument(currentPage != null);
			currentPage.addTask(task);
		}

		@Override
		@Deprecated
		public void showInventory(Player p) {
			throw new UnsupportedOperationException();
		}

		@Override
		public void updateInventory() {
			for (Player viewer : InventoryAPI.getViewingPlayers(getInventory())) {
				viewer.updateInventory();
			}
		}

	}

}
