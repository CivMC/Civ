package vg.civcraft.mc.civmodcore.playersettings;

import java.util.UUID;

/**
 * Allows listening for value changes in a setting
 */
public interface SettingChangeListener<T> {
	
	public void handle(UUID player, PlayerSetting<T> setting, T oldValue, T newValue);

}
