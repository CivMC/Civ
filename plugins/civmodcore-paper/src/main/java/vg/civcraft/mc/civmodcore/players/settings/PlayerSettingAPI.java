package vg.civcraft.mc.civmodcore.players.settings;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.experimental.UtilityClass;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
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

	private static final String FILE_NAME = "civ-player-settings.yml";

	private static final Map<String, PlayerSetting<?>> SETTINGS_BY_IDENTIFIER = new HashMap<>();

	private static final Map<String, List<PlayerSetting<?>>> SETTINGS_BY_PLUGIN = new HashMap<>();

	private static final MenuSection MAIN_MENU = new MenuSection("Config", "", null);

	/**
	 * @return GUI main menu
	 */
	public static MenuSection getMainMenu() {
		return MAIN_MENU;
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

	private static void loadValues(PlayerSetting<?> setting) {
		File folder = setting.getOwningPlugin().getDataFolder();
		if (!folder.isDirectory()) {
			return;
		}
		File file = new File(folder, FILE_NAME);
		if (!file.isFile()) {
			return;
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
		ConfigurationSection section = config.getConfigurationSection(setting.getIdentifier());
		if (section == null) {
			return;
		}
		for (String key : section.getKeys(false)) {
			setting.load(key, section.getString(key));
		}
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
		loadValues(setting);
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

	// TODO: While this deregisteres the settings, those settings then need to be removed from menus
	//    Maybe menus need a rework?
//	public static void deregisterPluginSettings(Plugin plugin) {
//		Preconditions.checkArgument(plugin != null);
//		Iteration.iterateThenClear(SETTINGS_BY_PLUGIN.get(plugin.getName()), (setting) ->
//				SETTINGS_BY_IDENTIFIER.remove(setting.getIdentifier()));
//		SETTINGS_BY_PLUGIN.remove(plugin.getName());
//	}

	/**
	 * Saves all values to their save files
	 */
	public static void saveAll() {
		for (Entry<String, List<PlayerSetting<?>>> pluginEntry : SETTINGS_BY_PLUGIN.entrySet()) {
			if (pluginEntry.getValue().isEmpty()) {
				continue;
			}
			File folder = pluginEntry.getValue().get(0).getOwningPlugin().getDataFolder();
			if (!folder.isDirectory()) {
				folder.mkdirs();
			}
			File file = new File(folder, FILE_NAME);
			YamlConfiguration config;
			if (file.isFile()) {
				config = YamlConfiguration.loadConfiguration(file);
			}
			else {
				config = new YamlConfiguration();
			}
			for (PlayerSetting<?> setting : pluginEntry.getValue()) {
				ConfigurationSection section;
				if (config.isConfigurationSection(setting.getIdentifier())) {
					section = config.getConfigurationSection(setting.getIdentifier());
				} else {
					section = config.createSection(setting.getIdentifier());
				}
				for (Entry<String, String> entry : setting.dumpAllSerialized().entrySet()) {
					section.set(entry.getKey(), entry.getValue());
				}
			}
			try {
				config.save(file);
			} catch (IOException e) {
				CivModCorePlugin.getInstance().severe("Failed to save settings", e);
			}
		}
	}

}
