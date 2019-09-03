package vg.civcraft.mc.civmodcore.playersettings;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.gui.MenuSection;

public abstract class StringInputSetting<T> extends PlayerSetting<T> {

	public StringInputSetting(JavaPlugin owningPlugin, T defaultValue, String name, String identifier, ItemStack gui, String description) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
	}

	public void handleMenuClick(Player player, MenuSection menu) {
		new MenuDialog(player, this, menu, "Invalid input");
	}
	
	public void setValueFromString(UUID player, String inputValue) {
		T value = deserialize(inputValue);
		setValue(player, value);
	}

	/**
	 * Input validation to confirm player entered values are not malformed
	 * 
	 * @param input Input string to test
	 * @return True if the input can be parsed as valid value, false otherwise
	 */
	public abstract boolean isValidValue(String input);

}
