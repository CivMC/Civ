package vg.civcraft.mc.civmodcore.playersettings.impl.collection;

import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import vg.civcraft.mc.civmodcore.playersettings.PlayerSetting;
import vg.civcraft.mc.civmodcore.playersettings.SettingTypeManager;

public class AbstractCollectionSetting<C extends Collection<T>, T> extends PlayerSetting<C> {

	private static final char SEPARATOR = ',';
	private static final String SEPARATOR_STRING = String.valueOf(SEPARATOR);
	private static final char ESCAPE = ';';
	private static final String ESCAPE_STRING = String.valueOf(ESCAPE);
	private static final String ESCAPE_REPLACE = ESCAPE_STRING + ESCAPE_STRING;
	private static final String SEPARATOR_REPLACE = ESCAPE_STRING + SEPARATOR_STRING;

	private PlayerSetting<T> elementSetting;
	private Function<C,C> newFunction;

	public AbstractCollectionSetting(JavaPlugin owningPlugin, C defaultValue, String name, String identifier,
			ItemStack gui, String description, Class<T> elementClass, Function<C,C> newFunction) {
		super(owningPlugin, defaultValue, name, identifier, gui, description);
		this.newFunction = newFunction;
		if (defaultValue == null) {
			defaultValue = newFunction.apply(null);
		}
		elementSetting = SettingTypeManager.getSetting(elementClass);
		if (elementSetting == null) {
			throw new IllegalArgumentException("Can not keep " + elementClass.getName()
					+ " in collection, because it was not registed in SettingTypeManager");
		}
	}

	public void addElement(UUID uuid, T element) {
		// need to clone list here to avoid problems with reused default values
		C collection = newFunction.apply(getValue(uuid));
		collection.add(element);
		setValue(uuid, collection);
	}

	public boolean removeElement(UUID uuid, T element) {
		C collection = getValue(uuid);
		return collection.remove(element);
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
	public C deserialize(String serial) {
		C result = newFunction.apply(null);
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
	public String serialize(C value) {
		StringBuilder sb = new StringBuilder();
		Iterator<T> iter = value.iterator();
		while (iter.hasNext()) {
			T element = iter.next();
			String serialized = elementSetting.serialize(element);
			String escaped = escape(serialized);
			sb.append(escaped);
			if (iter.hasNext()) {
				sb.append(SEPARATOR);
			}
		}
		return sb.toString();
	}

	@Override
	public String toText(C value) {
		StringBuilder sb = new StringBuilder();
		Iterator<T> iter = value.iterator();
		while (iter.hasNext()) {
			sb.append(elementSetting.toText(iter.next()));
			if (iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}

}
