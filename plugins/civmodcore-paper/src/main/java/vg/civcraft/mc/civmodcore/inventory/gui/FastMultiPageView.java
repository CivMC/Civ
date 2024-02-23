package vg.civcraft.mc.civmodcore.inventory.gui;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

/**
 * Modification to MultiPageView that only generates the clickables as needed for each page.
 * The "Fast" name comes from the fact that generating several pages of items upfront can be pretty taxing on the server
 */
public class FastMultiPageView {

	private Player p;
	private int currentPage = 0;
	private BiFunction<Integer, Integer, List<IClickable>> clickableSupplier;
	private String invName;
	private int rows;
	private IClickable[] extraMenuItems = new IClickable[7];

	public FastMultiPageView(Player p, BiFunction<Integer, Integer, List<IClickable>> clickableSupplier, String invName, int rows) {
		this.p = p;
		this.clickableSupplier = clickableSupplier;
		this.invName = invName;
		this.rows = rows;
	}

	/**
	 * Construct a clickable inventory containing the clickables given in the constructor, split up on different pages.
	 * The view will also include bach and forth buttons to navigate. This method should only be called repeatedly to
	 * change pages, if the clickables need to change just make a new instance
	 * 
	 * @return ClickableInventory of the current page
	 */
	private ClickableInventory constructInventory() {
		ClickableInventory ci = new ClickableInventory(rows * 9, invName);
		int contentSize = getContentSize();
		List<IClickable> tempPageClickables = clickableSupplier.apply(currentPage * contentSize, contentSize);
		// size may have changed
		while (tempPageClickables.isEmpty() && currentPage != 0) {
			// would show an empty page, so go to previous
			currentPage--;
			tempPageClickables = clickableSupplier.apply(currentPage * contentSize, contentSize + 1);
		}
		List<IClickable> pageClickables = tempPageClickables;
		// fill gui
		for (int i = 0; i < contentSize && i < Math.min(contentSize, pageClickables.size()); i++) {
			ci.setSlot(pageClickables.get(i), i);
		}
		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(back, ChatColor.GOLD + "Go to previous page");
			Clickable baCl = new Clickable(back) {

				@Override
				public void clicked(Player arg0) {
					if (currentPage > 0) {
						currentPage--;
					}
					showScreen();
				}
			};
			ci.setSlot(baCl, getContentSize());
		}
		// next button
		if (pageClickables.size() > contentSize) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ItemUtils.setDisplayName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if (pageClickables.size() > contentSize) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, getContentSize() + 8);
		}
		int extraSlot = getContentSize() + 2;
		for (IClickable click : extraMenuItems) {
			if (click != null) {
				ci.setSlot(click, extraSlot++);
			}
		}
		return ci;
	}

	private int getContentSize() {
		return (rows - 1) * 9;
	}

	/**
	 * Shows the current page
	 */
	public void showScreen() {
		ClickableInventory ci = constructInventory();
		ci.showInventory(p);
	}

	/**
	 * Allows setting a menu slot at the bottom of the gui. The slot must be a number between 0 and 6 (inclusive on both
	 * ends), because only 7 slots are available
	 * 
	 * @param click
	 *            Clickable to put in slot
	 * @param slot
	 *            Slot to put it in
	 */
	public void setMenuSlot(IClickable click, int slot) {
		if (slot < 0 || slot > 6) {
			throw new IllegalArgumentException("Slot for Multipageview menu item must be between 0 and 6");
		}
		extraMenuItems[slot] = click;
	}

	/**
	 * Force changes the page of this view. You will have to construct a new inventory for this to apply
	 * 
	 * @param page
	 *            Page to set to
	 */
	public void setPage(int page) {
		this.currentPage = page;
	}

}
