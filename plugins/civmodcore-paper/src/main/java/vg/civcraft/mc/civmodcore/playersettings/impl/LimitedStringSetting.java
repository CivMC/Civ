package vg.civcraft.mc.civmodcore.playersettings.impl;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class LimitedStringSetting extends StringSetting {

	private Set<String> validValues;
	private boolean caseSensitive;
	
	public LimitedStringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
								String description, Collection<String> validValues, boolean caseSensitive) {
		super(plugin, defaultValue, name, identifier, gui, description);
		this.validValues = new HashSet<>(validValues.size());
		for(String s : validValues) {
			if (caseSensitive) {
				this.validValues.add(s);
			} else {
				this.validValues.add(s.toLowerCase());
			}
		}
		this.caseSensitive = caseSensitive;
	}
	
	@Override
	public boolean isValidValue(String val) {
		if (!super.isValidValue(val)) {
			return false;
		}
		if (caseSensitive) {
			return validValues.contains(val);
		} else {
			return validValues.contains(val.toLowerCase());
		}
	}

}
