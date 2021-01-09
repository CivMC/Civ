package vg.civcraft.mc.civmodcore.playersettings.impl.collection;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class SetSetting <T> extends AbstractCollectionSetting<Set<T>, T> {

	public SetSetting(JavaPlugin owningPlugin, Set<T> defaultValue, String name, String identifier, ItemStack gui,
			String description, Class<T> elementClass) {
		super(owningPlugin, defaultValue, name, identifier, gui, description, elementClass, (c) -> {
			if (c == null) {
				return new HashSet<>();
			}
			return new HashSet<>(c);
		});
	}
	public SetSetting(JavaPlugin owningPlugin, String name, String identifier, ItemStack gui,
			String description, Class<T> elementClass) {
		this(owningPlugin, new HashSet<>(), name, identifier, gui, description, elementClass);
	}

}
