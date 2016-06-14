package vg.civcraft.mc.civmodcore.inventorygui;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Deprecated
public abstract class BlinkingClickable extends Clickable {
	private List<ItemStack> items;
	private int index;

	@Deprecated
	public BlinkingClickable(List<ItemStack> stacks) {
		super(stacks.get(0));
		this.items = stacks;
	}
	
	public ItemStack getNext() {
		index++;
		if (index == items.size()) {
			index = 0;
		}
		return items.get(index);
	}
	
	public abstract void clicked(Player p);

}
