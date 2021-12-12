package vg.civcraft.mc.civmodcore.players.settings;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.experimental.UtilityClass;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuOption;
import vg.civcraft.mc.civmodcore.players.settings.gui.MenuSection;
import vg.civcraft.mc.civmodcore.players.settings.impl.AltConsistentSetting;

/**
 * Allows creating settings, which will automatically be available in players
 * configuration GUI
 *
 */
@UtilityClass
public final class PlayerSettingAPI {

	private static final Map<String, PlayerSetting<?>> SETTINGS_BY_IDENTIFIER = new ConcurrentHashMap<>();

	private static final Map<String, List<PlayerSetting<?>>> SETTINGS_BY_PLUGIN = new ConcurrentHashMap<>();

	private static final MenuSection MAIN_MENU = new MenuSection("Config", "", null);

	/**
	 * @return GUI main menu
	 */
	public static MenuSection getMainMenu() {
		return MAIN_MENU;
	}

	public static Collection<PlayerSetting<?>> getAllSettings() {
		return Collections.unmodifiableCollection(SETTINGS_BY_IDENTIFIER.values());
	}

	/**
	 * Gets a setting by its identifier
	 * 
	 * @param identifier Identifier to get setting for
	 * @return Setting with the given identifier or null if no such setting exists
	 */
	public static PlayerSetting<?> getSetting(String identifier) {
		return SETTINGS_BY_IDENTIFIER.get(identifier);
	}

	/**
	 * Settings must be registered on every startup to be available. Identifiers
	 * must be unique globally.
	 * 
	 * If a setting had values assigned but is not registered on startup its old
	 * values will be left alone.
	 * 
	 * @param setting Setting to register
	 * @param menu Menu in which this value will appear
	 */
	public static void registerSetting(PlayerSetting<?> setting, MenuSection menu) {
		Preconditions.checkArgument(setting != null, "Player setting cannot be null.");
		if (setting instanceof AltConsistentSetting) {
			if (setting.canBeChangedByPlayer()) {
				menu.addItem(new MenuOption(menu, setting));
			}
			menu = null;
			setting = ((AltConsistentSetting<?,?>) setting).getWrappedSetting();
		}
		List<PlayerSetting<?>> pluginSettings = SETTINGS_BY_PLUGIN.computeIfAbsent(
				setting.getOwningPlugin().getName(),
				k -> new ArrayList<>());
		Preconditions.checkArgument(!pluginSettings.contains(setting),
				"Cannot register the same player setting twice.");
		SETTINGS_BY_IDENTIFIER.put(setting.getIdentifier(), setting);
		pluginSettings.add(setting);
		if (menu != null && setting.canBeChangedByPlayer()) {
			menu.addItem(new MenuOption(menu, setting));
		}
	}
}
