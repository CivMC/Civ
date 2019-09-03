package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LimitedStringSetting <T> extends StringSetting {

	private List<T> validValues;
	
	public LimitedStringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description, Collection<T> validValues) {
		super(plugin, defaultValue, name, identifier, gui, description);
		this.validValues = new ArrayList<>(validValues);
	}
	
	public abstract String convert(T t);

}
