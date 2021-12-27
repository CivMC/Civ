package vg.civcraft.mc.civmodcore.inventory.gui;

import org.bukkit.inventory.ItemStack;

/**
 * A possible option in a clickable inventory. Implement clicked(Player p) to do something whenever the item of this
 * clickable is clicked.
 *
 * @author Maxopoly
 */
public abstract class Clickable extends IClickable {

	protected ItemStack item;

	public Clickable(ItemStack item) {
		this.item = item;
	}

	/**
	 * @return Which item stack represents this clickable
	 */
	@Override
	public ItemStack getItemStack() {
		return item;
	}

	@Override
	public void addedToInventory(ClickableInventory inv, int slot) {
		// dont need anything for static representation
	}

}
