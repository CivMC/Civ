package vg.civcraft.mc.civmodcore.playersettings.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.itemHandling.ISUtils;

public class MenuSection extends MenuItem {

	private Map<String, MenuItem> content;

	private ItemStack itemRepresentation;

	public MenuSection(String name, MenuSection parent) {
		this(name, parent, new ItemStack(Material.BOOK));
	}

	public MenuSection(String name, MenuSection parent, ItemStack itemRepresentation) {
		super(name, parent);
		this.content = new TreeMap<>();
		this.itemRepresentation = itemRepresentation;
		ISUtils.setName(itemRepresentation, ChatColor.AQUA + name);
	}

	public void addItem(MenuItem item) {
		content.put(item.getName(), item);
	}
	
	public void createMenuSection(String name) {
		addItem(new MenuSection(name, this));
	}

	@Override
	public IClickable getMenuRepresentation(Player player) {
		return new Clickable(itemRepresentation) {

			@Override
			public void clicked(Player p) {
				showScreen(player);
			}
		};
	}

	public void showScreen(Player player) {
		List<IClickable> clickables = new LinkedList<>();
		for (MenuItem item : content.values()) {
			clickables.add(item.getMenuRepresentation(player));
		}
		MultiPageView pageView = new MultiPageView(player, clickables, getName(), true);
		ItemStack parentItem = new ItemStack(Material.ARROW);
		ISUtils.setName(parentItem, ChatColor.AQUA + "Go back to " + parent.getName());
		if (parent != null) {
			pageView.setMenuSlot(new Clickable(parentItem) {

				@Override
				public void clicked(Player p) {
					parent.showScreen(p);
				}
			}, 2);
		}
		pageView.showScreen();
	}

}
