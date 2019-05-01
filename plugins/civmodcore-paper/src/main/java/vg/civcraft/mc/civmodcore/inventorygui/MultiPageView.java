package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

/**
 * Utility to automate creating views, which have multiple pages and automatically adjust their size
 *
 */
public class MultiPageView {

	private Player p;
	private int currentPage;
	private List<IClickable> clickables;
	private String invName;
	private boolean adjustSize;
	private IClickable[] extraMenuItems;

	public MultiPageView(Player p, List<IClickable> clickables, String invName, boolean adjustSize) {
		currentPage = 0;
		this.p = p;
		this.clickables = clickables;
		this.invName = invName;
		this.adjustSize = adjustSize;
		extraMenuItems = new IClickable[7];
	}

	/**
	 * Construct a clickable inventory containing the clickables given in the constructor, split up on different pages.
	 * The view will also include bach and forth buttons to navigate. This method should only be called repeatedly to
	 * change pages, if the clickables need to change just make a new instance
	 * 
	 * @return ClickableInventory of the current page
	 */
	private ClickableInventory constructInventory() {
		ClickableInventory ci = new ClickableInventory(getRowAmount() * 9, invName);
		int contentSize = getContentSize();
		// size may have changed
		while (clickables.size() < contentSize * currentPage && currentPage != 0) {
			// would show an empty page, so go to previous
			currentPage--;
		}
		// fill gui
		for (int i = contentSize * currentPage; i < contentSize * (currentPage + 1) && i < clickables.size(); i++) {
			ci.setSlot(clickables.get(i), i - (contentSize * currentPage));
		}
		// back button
		if (currentPage > 0) {
			ItemStack back = new ItemStack(Material.ARROW);
			ISUtils.setName(back, ChatColor.GOLD + "Go to previous page");
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
		if ((getContentSize() * (currentPage + 1)) < clickables.size()) {
			ItemStack forward = new ItemStack(Material.ARROW);
			ISUtils.setName(forward, ChatColor.GOLD + "Go to next page");
			Clickable forCl = new Clickable(forward) {

				@Override
				public void clicked(Player arg0) {
					if ((getContentSize() * (currentPage + 1)) <= clickables.size()) {
						currentPage++;
					}
					showScreen();
				}
			};
			ci.setSlot(forCl, getContentSize() + 8);
		}
		int extraSlot = getContentSize() + 2;
		for (IClickable click : extraMenuItems) {
			if (click == null) {
				continue;
			}
			ci.setSlot(click, extraSlot++);
		}
		return ci;
	}

	/**
	 * @return How many rows the inventory has, including the navigation bar
	 */
	private int getRowAmount() {
		int clicks = clickables.size();
		if (clicks > 45 || !adjustSize) {
			return 6;
		}
		int extraRows = clicks / 9;
		if ((clicks % 9) != 0) {
			extraRows++;
		}
		// extra bottom menu row
		extraRows++;
		return extraRows;
	}

	/**
	 * @return How many slots in each page are reserved for content clickables
	 */
	private int getContentSize() {
		return (getRowAmount() - 1) * 9;
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
