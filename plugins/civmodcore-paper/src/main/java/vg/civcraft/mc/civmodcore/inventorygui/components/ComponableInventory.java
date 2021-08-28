package vg.civcraft.mc.civmodcore.inventorygui.components;

import java.util.List;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.inventorygui.ClickableInventory;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public class ComponableInventory extends ComponableSection {

	private ClickableInventory inv;
	private Player player;

	public ComponableInventory(String name, int rows, Player player) {
		super(rows * 9);
		this.player = player;
		inv = new ClickableInventory(getSize(), name);
	}

	public void show() {
		rebuild();
		updatePlayerView();
	}
	
	public void updatePlayerView() {
		for (int i = 0; i < content.size(); i++) {
			IClickable click = content.get(i);
			inv.setSlot(click, i);
		}
		inv.showInventory(player);
	}

	/**
	 * Updates the name of the inventory, closes the inventory and reopens it for
	 * the player. Note that the Minecraft protocol does not support changing the
	 * name of an open inventory, so the closing/opening which includes a client
	 * side cursor reset is unavoidable
	 * 
	 * @param name Name to update to
	 */
	public void setName(String name) {
		this.inv = new ClickableInventory(getSize(), name);
		inv.showInventory(player);
	}

	@Override
	protected void updateComponent(InventoryComponent component) {
		// copy of the implementation from ComponableSection, except that we also mirror
		// changes through to the ClickableInventory
		int offSet = 0;
		component.rebuild();
		List<IClickable> componentContent = component.getContent();
		for (int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots[i] == component) {
				IClickable click = componentContent.get(offSet++);
				this.content.set(i, click);
				this.inv.setSlot(click, i);
			}
		}
		inv.updateInventory();
	}

}
