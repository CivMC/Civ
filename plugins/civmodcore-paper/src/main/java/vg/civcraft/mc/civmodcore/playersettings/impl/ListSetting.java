package vg.civcraft.mc.civmodcore.playersettings.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.SettingTypeManager;

public class ListSetting<T> extends PlayerSetting<List<T>> {

	private static final char SEPARATOR = ',';
	private static final String SEPARATOR_STRING = String.valueOf(SEPARATOR);
	private static final char ESCAPE = ';';
	private static final String ESCAPE_STRING = String.valueOf(ESCAPE);
	private static final String ESCAPE_REPLACE = ESCAPE_STRING + ESCAPE_STRING;
	private static final String SEPARATOR_REPLACE = ESCAPE_STRING + SEPARATOR_STRING;

	private PlayerSetting<T> elementSetting;

	public ListSetting(JavaPlugin owningPlugin, List<T> defaultValue, String name, String identifier, ItemStack gui,
			String description, Class<T> elementClass) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
		if (defaultValue == null) {
			defaultValue = new ArrayList<>();
		}
		elementSetting = SettingTypeManager.getSetting(elementClass);
		if (elementSetting == null) {
			throw new IllegalArgumentException("Can not keep " + elementClass.getName()
					+ " in list, because it was not registed in SettingTypeManager");
		}
	}
	
	public void addElement(UUID uuid, T element) {
		//need to clone list here to avoid problems with reused default values
		List <T> list = new ArrayList<>(getValue(uuid));
		list.add(element);
		setValue(uuid, list);
	}
	
	public boolean removeElement(UUID uuid, T element) {
		List <T> list = getValue(uuid);
		return list.remove(element);
	}
	
	public boolean contains(UUID uuid, T element) {
		return getValue(uuid).contains(element);
	}

	@Override
	public boolean isValidValue(String input) {
		boolean escape = false;
		int startingIndex = 0;
		for (int i = 0; i < input.length(); i++) {
			if (escape) {
				escape = false;
				continue;
			}
			if (input.charAt(i) == ESCAPE) {
				escape = true;
				continue;
			}
			if (input.charAt(i) == SEPARATOR) {
				String element = input.substring(startingIndex, i);
				if (!elementSetting.isValidValue(element)) {
					return false;
				}
				startingIndex = i + 1;
			}
		}
		// final element not checked by the loop, because there's no comma after it
		return elementSetting.isValidValue(input.substring(startingIndex, input.length()));
	}

	@Override
	public List<T> deserialize(String serial) {
		List<T> result = new ArrayList<>();
		boolean escape = false;
		int startingIndex = 0;
		for (int i = 0; i < serial.length(); i++) {
			if (escape) {
				escape = false;
				continue;
			}
			if (serial.charAt(i) == ESCAPE) {
				escape = true;
				continue;
			}
			if (serial.charAt(i) == SEPARATOR) {
				String element = serial.substring(startingIndex, i);
				element = removeEscapes(element);
				result.add(elementSetting.deserialize(element));
				startingIndex = i + 1;
			}
		}
		// final element not checked by the loop, because there's no comma after it
		result.add(elementSetting.deserialize(removeEscapes(serial.substring(startingIndex, serial.length()))));
		return result;
	}

	private static String removeEscapes(String val) {
		return val.replaceAll(SEPARATOR_REPLACE, SEPARATOR_REPLACE).replaceAll(ESCAPE_REPLACE, ESCAPE_STRING);
	}

	private static String escape(String val) {
		return val.replaceAll(ESCAPE_STRING, ESCAPE_REPLACE).replaceAll(SEPARATOR_STRING, SEPARATOR_REPLACE);
	}

	@Override
	public String serialize(List<T> value) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.size() - 1; i++) {
			T element = value.get(i);
			String serialized = elementSetting.serialize(element);
			String escaped = escape(serialized);
			sb.append(escaped);
			sb.append(SEPARATOR);
		}
		sb.append(escape(elementSetting.serialize(value.get(value.size() - 1))));
		return sb.toString();
	}

	@Override
	public String toText(List<T> value) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < value.size() - 1; i++) {
			sb.append(elementSetting.toText(value.get(i)));
			sb.append(", ");
		}
		sb.append(elementSetting.toText(value.get(value.size() - 1)));
		return sb.toString();
	}

}
