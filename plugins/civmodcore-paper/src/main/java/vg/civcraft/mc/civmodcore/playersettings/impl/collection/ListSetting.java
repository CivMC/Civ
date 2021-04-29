package vg.civcraft.mc.civmodcore.playersettings.impl.collection;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class ListSetting <T> extends AbstractCollectionSetting<List<T>, T> {

	public ListSetting(JavaPlugin owningPlugin, List<T> defaultValue, String name, String identifier, ItemStack gui,
			String description, Class<T> elementClass) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, elementClass, (c) -> {
			if (c == null) {
				return new ArrayList<>();
			}
			return new ArrayList<>(c);
		});
	}
	public ListSetting(JavaPlugin owningPlugin, String name, String identifier, ItemStack gui,
			String description, Class<T> elementClass) {
		this(owningPlugin, new ArrayList<>(), name, identifier, gui, description, elementClass);
	}

}
