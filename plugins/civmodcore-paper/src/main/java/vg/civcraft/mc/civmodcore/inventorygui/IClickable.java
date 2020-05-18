package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class IClickable {

	/**
	 * General method called whenever this clickable is clicked with a type that did
	 * not a have a special implementation provided
	 *
	 * @param p Player who clicked
	 */
	protected abstract void clicked(Player p);

	/**
	 * Called when a player double clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onDoubleClick(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player drops one of this clickable (in default keybinds
	 * pressing Q while hovering the slot), overwrite to define special behavior for
	 * this
	 * 
	 * @param p Player who clicked
	 */
	protected void onDrop(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player drops this clickable stack (in default keybinds pressing
	 * Q while holding CTRL and hovering the slot), overwrite to define special
	 * behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onControlDrop(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player left clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onLeftClick(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player right clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onRightClick(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player middle (mouse wheell) clicks this clickable, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onMiddleClick(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player left clicks this clickable while holding shift, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onShiftLeftClick(Player p) {
		clicked(p);
	}

	/**
	 * Called when a player right clicks this clickable while holding shift, overwrite to define
	 * special behavior for this
	 * 
	 * @param p Player who clicked
	 */
	protected void onShiftRightClick(Player p) {
		clicked(p);
	}

	public void handleClick(Player p, ClickType type) {
		switch (type) {
		case CONTROL_DROP:
			onControlDrop(p);
			break;
		case DOUBLE_CLICK:
			onDoubleClick(p);
			break;
		case DROP:
			onDrop(p);
			break;
		case LEFT:
			onLeftClick(p);
			break;
		case MIDDLE:
			onMiddleClick(p);
			break;
		case RIGHT:
			onRightClick(p);
			break;
		case SHIFT_LEFT:
			onShiftLeftClick(p);
			break;
		case SHIFT_RIGHT:
			onShiftRightClick(p);
			break;
		case CREATIVE:
		case UNKNOWN:
		case WINDOW_BORDER_LEFT:
		case WINDOW_BORDER_RIGHT:
		case NUMBER_KEY:
			clicked(p);
			break;
		default:
			clicked(p);
			break;
		}
	}

	/**
	 * @return Which item stack represents this clickable when it is initially
	 *         loaded into the inventory
	 */
	public abstract ItemStack getItemStack();

	/**
	 * Called when this instance is added to an inventory so it can do something if
	 * desired
	 * 
	 * @param inv  Inventory it was added to
	 * @param slot Slot in which it was added
	 */
	public abstract void addedToInventory(ClickableInventory inv, int slot);

}
