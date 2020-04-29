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
		for(int i = 0; i < content.size(); i++) {
			inv.setSlot(content.get(i), i);
		}
		inv.showInventory(player);
	}
	
	@Override
	void updateComponent(InventoryComponent component) {
		//copy of the implementation from ComponableSection, except that we also mirror changes through to the ClickableInventory
		int offSet = 0;
		component.rebuild();
		List <IClickable> componentContent = component.getContent();
		for(int i = 0; i < occupiedSlots.length; i++) {
			if (occupiedSlots [i] == component) {
				IClickable click = componentContent.get(offSet++);
				this.content.set(i, click);
				this.inv.setSlot(click, i);
			}
		}
		inv.updateInventory();
	}

}
