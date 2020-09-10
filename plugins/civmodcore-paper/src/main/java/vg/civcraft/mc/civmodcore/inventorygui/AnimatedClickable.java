package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import vg.civcraft.mc.civmodcore.CivModCorePlugin;

public class AnimatedClickable extends IClickable {

	private List<ItemStack> items;
	private long timing;
	private int currentPos;

	public AnimatedClickable(List<ItemStack> stacks, long timing) {
		if (stacks.isEmpty()) {
			throw new IllegalArgumentException("Can't create blinking clickable with empty item list");
		}
		this.items = stacks;
		this.timing = timing;
		this.currentPos = 0;
	}

	public ItemStack getNext() {
		if (++currentPos == items.size()) {
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
		BukkitTask task = new BukkitRunnable() {
			@Override
			public void run() {
				inv.setItem(getNext(), slot);
			}
		}.runTaskTimer(CivModCorePlugin.getInstance(), timing, timing);
		inv.registerTask(task);
	}

	/**
	 * @return How often this instance will switch it's item representation
	 */
	public long getTiming() {
		return timing;
	}

}
