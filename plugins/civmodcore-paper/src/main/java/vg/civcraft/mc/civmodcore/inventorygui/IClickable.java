package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IClickable {

	/**
	 * What is done whenever this element is clicked
	 *
	 * @param p
	 *            Player who clicked
	 */
	void clicked(Player p);

	/**
	 * @return Which item stack represents this clickable when it is initially
	 *         loaded into the inventory
	 */
	ItemStack getItemStack();

	/**
	 * Called when this instance is added to an inventory so it can do something
	 * if desired
	 * 
	 * @param inv
	 *            Inventory it was added to
	 * @param slot
	 *            Slot in which it was added
	 */
	void addedToInventory(ClickableInventory inv, int slot);

}
