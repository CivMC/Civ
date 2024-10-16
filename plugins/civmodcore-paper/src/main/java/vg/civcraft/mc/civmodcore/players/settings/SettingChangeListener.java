package vg.civcraft.mc.civmodcore.players.settings;

import java.util.UUID;

/**
 * Allows listening for value changes in a setting
 */
public interface SettingChangeListener<T> {
	
	void handle(UUID player, PlayerSetting<T> setting, T oldValue, T newValue);

}
