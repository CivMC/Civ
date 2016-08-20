package vg.civcraft.mc.civmodcore.inventorygui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * A possible option in a clickable inventory. Implement clicked(Player p) to do
 * something whenever the item of this clickable is clicked.
 *
 * @author Maxopoly
 */
public abstract class Clickable {

	protected ItemStack item;

	public Clickable(ItemStack item) {
		this.item = item;
	}

	/**
	 * What is done whenever this element is clicked
	 *
	 * @param p Player who clicked
	 */
	public abstract void clicked(Player p);

	/**
	 * @return Which item stack represents this clickable
	 */
	public ItemStack getItemStack() {
		return item;
	}

}
