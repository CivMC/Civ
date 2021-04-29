package org.ipvp.canvas.type;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.ipvp.canvas.Menu;
import org.ipvp.canvas.slot.DefaultSlot;
import org.ipvp.canvas.slot.Slot;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.inventory.InventoryUtils;
import vg.civcraft.mc.civmodcore.inventory.gui.canvas.MenuUtils;
import vg.civcraft.mc.civmodcore.util.NullUtils;

public abstract class AbstractCivMenu extends AbstractMenu {

	private static final Field VIEWERS_FIELD;
	private static final Field SLOTS_FIELD;
	private static final Method UPDATE_INV_CONTENTS_METHOD;

	static {
		VIEWERS_FIELD = FieldUtils.getDeclaredField(AbstractMenu.class, "viewers", true);
		SLOTS_FIELD = FieldUtils.getDeclaredField(AbstractMenu.class, "slots", true);
		UPDATE_INV_CONTENTS_METHOD = MethodUtils.getMatchingMethod(AbstractMenu.class,
				"updateInventoryContents", Player.class, Inventory.class);
		UPDATE_INV_CONTENTS_METHOD.setAccessible(true);
	}

	private final Component title;
	private OpenHandler openHandler;
	private UpdateHandler updateHandler;
	protected boolean callCloseHandlerRegardless;

	protected AbstractCivMenu(final Component title,
							  final int slots,
							  final Menu parent,
							  final boolean redraw) {
		super(null, slots, parent, redraw);
		if (!InventoryUtils.isValidChestSize(slots)) {
			throw new IllegalArgumentException("Not a valid chest size.");
		}
		this.title = ChatUtils.isNullOrEmpty(title) ? InventoryType.CHEST.defaultTitle() : title;
	}

	protected AbstractCivMenu(final Component title,
							  final InventoryType type,
							  final Menu parent,
							  final boolean redraw) {
		super(null, type, parent, redraw);
		this.title = ChatUtils.isNullOrEmpty(title) ? type.defaultTitle() : title;
		this.inventorySlots = type.getDefaultSize();
	}

	/**
	 * @return Returns a clone of this menu's title.
	 */
	protected Component getTitle() {
		return ChatUtils.cloneComponent(this.title);
	}

	/**
	 * @return Retrieves this menu's open event handler.
	 */
	public Optional<OpenHandler> getOpenHandler() {
		return Optional.ofNullable(this.openHandler);
	}

	/**
	 * Sets the open event handler for this menu. Use
	 * {@link MenuUtils#overrideOpenHandler(AbstractCivMenu, OpenHandler, boolean)} instead if you'd prefer to
	 * override the event handler rather than replace it.
	 *
	 * @param openHandler The open event handler to set for this menu.
	 */
	public void setOpenHandler(final OpenHandler openHandler) {
		this.openHandler = openHandler;
	}

	/**
	 * @return Retrieves this menu's update event handler.
	 */
	public Optional<UpdateHandler> getUpdateHandler() {
		return Optional.ofNullable(this.updateHandler);
	}

	/**
	 * Sets the update event handler for this menu. Use
	 * {@link MenuUtils#overrideUpdateHandler(AbstractCivMenu, UpdateHandler, boolean)} instead if you'd prefer to
	 * override the event handler rather than replace it.
	 *
	 * @param updateHandler The update event handler to set for this menu.
	 */
	public void setUpdateHandler(final UpdateHandler updateHandler) {
		this.updateHandler = updateHandler;
	}

	/**
	 * Sets the close event handler for this menu. Use
	 * {@link MenuUtils#overrideCloseHandler(AbstractCivMenu, CloseHandler, boolean)} instead if you'd prefer to
	 * override the event handler rather than replace it.
	 *
	 * @param closeHandler The close event handler to set for this menu.
	 */
	@Override
	public void setCloseHandler(final CloseHandler closeHandler) {
		super.setCloseHandler(closeHandler);
	}

	/**
	 * @return Returns whether or not this menu will invoke its close event handler regardless of whether
	 *         {@link #closedByPlayer(Player, boolean)}'s second parameter, "triggerCloseHandler", is false.
	 */
	public boolean isCallingCloseHandlerRegardless() {
		return this.callCloseHandlerRegardless;
	}

	/**
	 * @param callCloseHandlerRegardless Set this to true if you want this menu to invoke its close event handler
	 *                                   regardless of whether {@link #closedByPlayer(Player, boolean)}'s second
	 *                                   parameter, "triggerCloseHandler", is false.
	 */
	public void setCallCloseHandlerRegardless(final boolean callCloseHandlerRegardless) {
		this.callCloseHandlerRegardless = callCloseHandlerRegardless;
	}

	/**
	 * @return Returns the total slot count for this menu.
	 */
	protected int getSlotCount() {
		return this.inventorySlots;
	}

	/**
	 * @return Returns the underlying viewers via reflection.
	 */
	@SuppressWarnings("unchecked")
	protected final Set<MenuHolder> getRawViewers() {
		try {
			return (Set<MenuHolder>) FieldUtils.readField(VIEWERS_FIELD, this);
		}
		catch (final IllegalAccessException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * @return Returns the underlying slots via reflection.
	 */
	protected final DefaultSlot[] getRawSlots() {
		try {
			return (DefaultSlot[]) FieldUtils.readField(SLOTS_FIELD, this);
		}
		catch (final IllegalAccessException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * Switches from this menu to a given menu.
	 *
	 * @param viewer The viewer to switch.
	 * @param otherMenu The other menu to switch to.
	 */
	public void openOtherMenu(final Player viewer, final AbstractCivMenu otherMenu) {
		if (otherMenu == null) {
			viewer.closeInventory();
			return;
		}
		otherMenu.open(viewer);
	}

	/**
	 * Opens this menu's parent, or closes if it doesn't exist.
	 *
	 * @param viewer The viewer to navigate.
	 */
	public void openParent(final Player viewer) {
		final var parent = getParent().orElse(null);
		if (parent == null) {
			viewer.closeInventory();
			return;
		}
		if (parent instanceof AbstractCivMenu) {
			((AbstractCivMenu) parent).openOtherMenu(viewer, this);
			return;
		}
		parent.open(viewer);
	}

	/**
	 * Basically a carbon copy of {@link AbstractMenu#closedByPlayer(Player, boolean)}, except
	 * for the additional logic of {@link #callCloseHandlerRegardless} to force the menu to call
	 * the close handler even if {@code triggerCloseHandler} is false.
	 *
	 * @param viewer The closing viewer.
	 * @param triggerCloseHandler Whether to call the close handler, which defers to
	 *                            {@link #callCloseHandlerRegardless}.
	 */
	@Override
	public void closedByPlayer(final Player viewer, final boolean triggerCloseHandler) {
		final var currentInventory = viewer.getOpenInventory().getTopInventory().getHolder();
		final var viewers = getRawViewers();
		if (!(currentInventory instanceof MenuHolder)
				|| !viewers.contains(currentInventory)) {
			return;
		}
		if (triggerCloseHandler || this.callCloseHandlerRegardless) {
			getCloseHandler().ifPresent(handler -> handler.close(viewer, this));
		}
		viewers.remove((MenuHolder) currentInventory);
	}

	/**
	 * @param viewer The viewer to update this menu for.
	 */
	@Override
	public void update(final Player viewer) throws IllegalStateException {
		if (!isOpen(viewer)) {
			return;
		}

		final var currentHolder = viewer.getOpenInventory().getTopInventory().getHolder();
		final var currentInventory = NullUtils.isNotNull(currentHolder).getInventory();

		for (final Slot slot : getRawSlots()) {
			currentInventory.setItem(slot.getIndex(), slot.getItem(viewer));
		}

		getUpdateHandler().ifPresent(handler -> handler.handle(viewer, this));
		viewer.updateInventory();
	}

	/**
	 * Basically a carbon copy of {@link AbstractMenu#open(Player)}, which is needed to override the
	 * {@code createInventory()} method, but it's private so the entire function needs to be copied,
	 * alongside its other dependent private methods.
	 */
	@Override
	public void open(final Player viewer) {
		final var currentHolder = viewer.getOpenInventory().getTopInventory().getHolder();
		final MenuHolder holder;
		if (currentHolder instanceof MenuHolder) {
			holder = (MenuHolder) currentHolder;
			final var currentMenu = holder.getMenu();

			if (currentMenu == this) {
				return;
			}

			Inventory inventory;
			if (isRedraw() && Objects.equals(getDimensions(), currentMenu.getDimensions())) {
				inventory = holder.getInventory();
				if (currentMenu instanceof AbstractMenu) {
					((AbstractMenu) currentMenu).closedByPlayer(viewer, false);
				}
			}
			else {
				currentMenu.close(viewer);
				inventory = createInventory(holder);
				holder.setInventory(inventory);
				viewer.openInventory(inventory);
			}

			updateInventoryContents(viewer, inventory);
			holder.setMenu(this);
		}
		else {
			// Create new MenuHolder for the player
			holder = new MenuHolder(viewer, this);
			final var inventory = createInventory(holder);
			updateInventoryContents(viewer, inventory);
			holder.setInventory(inventory);
			viewer.openInventory(inventory);
		}
		getRawViewers().add(holder);
		getOpenHandler().ifPresent(handler -> handler.handle(viewer, this));
	}

	/**
	 * Basically a carbon copy of {@link AbstractMenu}'s version, but uses a component title instead.
	 *
	 * @param holder The menu's inventory holder.
	 * @return Returns the new inventory.
	 */
	protected Inventory createInventory(final InventoryHolder holder) {
		if (this.inventoryType == null) {
			return Bukkit.createInventory(holder, this.inventorySlots, this.title);
		}
		return Bukkit.createInventory(holder, this.inventoryType, this.title);
	}

	/**
	 * Invokes {@link AbstractMenu}'s version via reflection.
	 *
	 * @param viewer The viewer to update.
	 * @param inventory The inventory to update.
	 */
	protected void updateInventoryContents(final Player viewer, final Inventory inventory) {
		try {
			UPDATE_INV_CONTENTS_METHOD.invoke(this, viewer, inventory);
		}
		catch (final InvocationTargetException | IllegalAccessException exception) {
			throw new RuntimeException(exception);
		}
	}

}
