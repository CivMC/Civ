package vg.civcraft.mc.civmodcore.playersettings.gui;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.inventorygui.Clickable;
import vg.civcraft.mc.civmodcore.inventorygui.IClickable;
import vg.civcraft.mc.civmodcore.inventorygui.MultiPageView;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.PlayerSettingAPI;

public class MenuSection extends MenuItem {

	private final Map<String, MenuItem> content;

	private final ItemStack itemRepresentation;

	public MenuSection(String name, String description, MenuSection parent) {
		this(name, description, parent, new ItemStack(Material.BOOK));
	}

	public MenuSection(String name, String description, MenuSection parent, ItemStack itemRepresentation) {
		super(name, parent);
		this.content = new TreeMap<>();
		this.itemRepresentation = itemRepresentation;
		ItemAPI.setDisplayName(itemRepresentation, ChatColor.AQUA + name);
		ItemAPI.addLore(itemRepresentation, ChatColor.GOLD + description);
	}

	public void addItem(MenuItem item) {
		content.put(item.getName(), item);
	}

	public Collection<MenuItem> getItems() {
		return this.content.values();
	}
	
	public MenuSection createMenuSection(String name, String description) {
		MenuSection section = new MenuSection(name, description, this);
		addItem(section);
		return section;
	}

	public MenuSection createMenuSection(String name, String description, ItemStack itemRepresentation) {
		MenuSection section = new MenuSection(name, description, this, itemRepresentation);
		addItem(section);
		return section;
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
		List<IClickable> clickables = new ArrayList<>(content.size());
		for (MenuItem item : content.values()) {
			clickables.add(item.getMenuRepresentation(player));
		}
		MultiPageView pageView = new MultiPageView(player, clickables, getName(), true);
		if (parent != null) {
			ItemStack parentItem = new ItemStack(Material.ARROW);
			ItemAPI.setDisplayName(parentItem, ChatColor.AQUA + "Go back to " + parent.getName());
			pageView.setMenuSlot(new Clickable(parentItem) {

				@Override
				public void clicked(Player p) {
					parent.showScreen(p);
				}
			}, 2);
		}
		pageView.showScreen();
	}

	/**
	 * Registers this menu with its parent.
	 */
	public void registerToParentMenu() {
		if (this.parent != null) {
			this.parent.addItem(this);
		}
	}

	/**
	 * Registers a setting with this menu.
	 *
	 * @param setting The setting to register.
	 */
	public void registerSetting(PlayerSetting<?> setting) {
		Preconditions.checkArgument(setting != null);
		PlayerSettingAPI.registerSetting(setting, this);
	}

}
