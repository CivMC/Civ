package vg.civcraft.mc.civmodcore.playersettings.gui;

import org.bukkit.entity.Player;

import vg.civcraft.mc.civmodcore.inventorygui.IClickable;

public abstract class MenuItem {
	
	protected final String name;
	protected final MenuSection parent;
	
	public MenuItem(String name, MenuSection parent) {
		this.name = name;
		this.parent = parent;
	}
	
	public abstract IClickable getMenuRepresentation(Player player);
	
	public String getName() {
		return name;
	}
	
	public MenuSection getParent() {
		return parent;
	}

}
