package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class AnimatedClickable implements IClickable {

	private List<ItemStack> items;
	private long timing;
	private int currentPos;

	public AnimatedClickable(List<ItemStack> stacks, long timing) {
		this.items = stacks;
		this.timing = timing;
		this.currentPos = 0;
		if (stacks.size() == 0) {
			throw new IllegalArgumentException("Can't create blinking clickable with empty item list");
		}
	}

	public ItemStack getNext() {
		currentPos++;
		if (currentPos == items.size()) {
			currentPos = 0;
		}
		return items.get(currentPos);
	}

	@Override
	public void clicked(Player p) {
	}

	@Override
	public ItemStack getItemStack() {
		return items.get(0);
	}

	@Override
	public void addedToInventory(final ClickableInventory inv, final int slot) {
		// Schedule swapping out of item
		new BukkitRunnable() {

			@Override
			public void run() {
				inv.setItem(getNext(), slot);
			}
		}.runTaskTimer(CivModCorePlugin.getInstance(), timing, timing);

	}

	/**
	 * @return How often this instance will switch it's item representation
	 */
	public long getTiming() {
		return timing;
	}
}
