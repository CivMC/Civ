package vg.civcraft.mc.civmodcore.inventory.gui;

import javax.annotation.Nonnull;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class IClickable {

	/**
	 * @return Which item stack represents this clickable when it is initially loaded into the inventory.
	 */
	@Nonnull
	public abstract ItemStack getItemStack();

	/**
	 * Called when this instance is added to an inventory so it can do something if
	 * desired
	 *
	 * @param inventory Inventory it was added to
	 * @param slot Slot in which it was added
	 */
	public abstract void addedToInventory(@Nonnull final ClickableInventory inventory, final int slot);

	/**
	 * General method called whenever this clickable is clicked with a type that did
	 * not a have a special implementation provided
	 *
	 * @param clicker Player who clicked
	 */
	protected abstract void clicked(@Nonnull final Player clicker);

	/**
	 * Called when a player double clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onDoubleClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player drops one of this clickable (in default keybinds
	 * pressing Q while hovering the slot), overwrite to define special behavior for
	 * this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onDrop(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player drops this clickable stack (in default keybinds pressing
	 * Q while holding CTRL and hovering the slot), overwrite to define special
	 * behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onControlDrop(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player left clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onLeftClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player right clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onRightClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player middle (mouse wheell) clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onMiddleClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player left clicks this clickable while holding shift, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onShiftLeftClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	/**
	 * Called when a player right clicks this clickable while holding shift, overwrite to define
	 * special behavior for this
	 * 
	 * @param clicker Player who clicked
	 */
	protected void onShiftRightClick(@Nonnull final Player clicker) {
		clicked(clicker);
	}

	public void handleClick(@Nonnull final Player clicker,
							@Nonnull final ClickType type) {
		switch (type) {
			case CONTROL_DROP -> onControlDrop(clicker);
			case DOUBLE_CLICK -> onDoubleClick(clicker);
			case DROP -> onDrop(clicker);
			case LEFT -> onLeftClick(clicker);
			case MIDDLE -> onMiddleClick(clicker);
			case RIGHT -> onRightClick(clicker);
			case SHIFT_LEFT -> onShiftLeftClick(clicker);
			case SHIFT_RIGHT -> onShiftRightClick(clicker);
			default -> clicked(clicker);
		}
	}

}
