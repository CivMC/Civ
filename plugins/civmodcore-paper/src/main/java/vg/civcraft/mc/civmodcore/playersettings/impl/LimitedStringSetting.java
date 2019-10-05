package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class LimitedStringSetting extends StringSetting {

	private Set<String> validValues;
	private boolean caseSensitive;
	
	public LimitedStringSetting(JavaPlugin plugin, String defaultValue, String name, String identifier, ItemStack gui,
			String description, Collection<String> validValues, boolean caseSensitive) {
		super(plugin, defaultValue, name, identifier, gui, description);
		this.validValues = new HashSet<>();
		for(String s : validValues) {
			if (!caseSensitive) {
				s = s.toLowerCase();
			}
			this.validValues.add(s);
		}
		this.caseSensitive = caseSensitive;
	}
	
	@Override
	public boolean isValidValue(String val) {
		if (!super.isValidValue(val)) {
			return false;
		}
		return validValues.contains(caseSensitive ? val : val.toLowerCase());
	}

}
