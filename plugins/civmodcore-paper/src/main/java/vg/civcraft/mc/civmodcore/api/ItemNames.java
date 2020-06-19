package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.ResourceUtils;
import vg.civcraft.mc.civmodcore.util.TextUtil;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * Class that loads and store item names. Replaces NiceNames.
 * */
public final class ItemNames {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemNames.class.getSimpleName());

	private static final String CUSTOM_ITEM_NBT = "CustomItem";

	private static final Map<NamespacedKey, String> ITEM_NAMES = new HashMap<>();

	/**
	 * Resets all item names, custom item names included.
	 * */
	public static void resetItemNames() {
		ITEM_NAMES.clear();
	}

	/**
	 * Loads item names from configurable files and requests any custom item names programmatically from plugins.
	 *
	 * @param plugin The CivModCore instance plugin.
	 * */
	public static void loadItemNames(CivModCorePlugin plugin) {
		resetItemNames();
		// Load material names from materials.csv
		if (!ResourceUtils.iterateResourceLines(plugin, "/materials.csv", line -> {
			String [] values = line.split(",");
			// If there's not at least three values (slug, name) then skip
			if (values.length < 2) {
				LOGGER.warn("This material row does not have enough data: " + line);
				return;
			}
			// If a material cannot be found by the slug given, skip
			Material material = Material.getMaterial(values[0]);
			if (material == null) {
				LOGGER.warn("Could not find a material on this line: " + line);
				return;
			}
			// If the name is empty, skip
			String name = TextUtil.parseColor(values[1]);
			if (!registerName(material.getKey(), name)) {
				LOGGER.warn("This material has not been given a name: " + line);
				//return;
			}
		})) {
			LOGGER.warn("Could not load materials from materials.csv");
		}
		else {
			LOGGER.info("Loaded a total of " + ITEM_NAMES.size() + " item names from materials.csv");
		}
	}

	/**
	 * Registers an item name to a particular key. You can also use this to override the names of Minecraft's materials.
	 * It is recommended you use {@link NamespacedKey#NamespacedKey(Plugin, String)} to create a key for your plugin's
	 * custom item, however you can also use {@link NamespaceAPI#fromString(String)}.
	 *
	 * @param key The key to set the item name to.
	 * @param name The item name to set.
	 * @return Returns true if the name was set.
	 */
	public static boolean registerName(NamespacedKey key, String name) {
		if (key == null || Strings.isNullOrEmpty(name)) {
			return false;
		}
		ITEM_NAMES.put(key, name);
		return true;
	}

	/**
	 * Removes a item name from the registry. You are not required to do this when your plugin disables.
	 *
	 * @param key The key to deregister.
	 */
	public static void deregisterName(NamespacedKey key) {
		ITEM_NAMES.remove(key);
	}

	/**
	 * Gets the name of an item based off a material, e.g: POLISHED_GRANITE to Polished Granite
	 *
	 * @param material The material to get the name of.
	 * @return Returns the material name.
	 */
	public static String getItemName(Material material) {
		if (material == null) {
			throw new IllegalArgumentException("Cannot retrieve name of invalid material.");
		}
		String name = ITEM_NAMES.get(material.getKey());
		if (Strings.isNullOrEmpty(name)) {
			return material.name();
		}
		return name;
	}

	/**
	 * Gets the name of an item either based off its material or its custom item tag.
	 *
	 * @param item The item to get the name of.
	 * @return Returns the item's name.
	 */
	public static String getItemName(ItemStack item) {
		if (item == null) {
			throw new IllegalArgumentException("Cannot retrieve name of invalid item.");
		}
		NamespacedKey customItem = getCustomItemKey(item);
		if (customItem != null) {
			String name = ITEM_NAMES.get(customItem);
			if (Strings.isNullOrEmpty(name)) {
				return ChatColor.RED + "UNREGISTERED CUSTOM ITEM MATERIAL, FIX THIS!!";
			}
			return name;
		}
		return getItemName(item.getType());
	}

	/**
	 * Attached custom item data to an item.
	 *
	 * @param item The item to convert into a custom item.
	 * @param key The custom item key to attach.
	 * @param overrideDisplayName Whether to set the display name to the custom item name.
	 * @return Returns the new custom item. Use this in lieu of the given item.
	 */
	public static ItemStack convertToCustomItem(ItemStack item, NamespacedKey key, boolean overrideDisplayName) {
		if (key == null) {
			throw new IllegalArgumentException("Cannot convert to custom item with an null key.");
		}
		if (!ITEM_NAMES.containsKey(key)) {
			throw new IllegalArgumentException("Cannot convert to custom item with a non registered key.");
		}
		item = NBTCompound.processItem(item, nbt -> {
			nbt.setString(CUSTOM_ITEM_NBT, key.toString());
		});
		if (overrideDisplayName) {
			ItemAPI.handleItemMeta(item, (ItemMeta meta) -> {
				meta.setDisplayName(ChatColor.RESET + ITEM_NAMES.get(key));
				return true;
			});
		}
		return item;
	}

	/**
	 * Determines whether an item has a display name. Use this in lieu of {@link ItemMeta#hasDisplayName()} as this is
	 * is compatible with custom item data, which may be set to the item's display name.
	 *
	 * @param item The item to check.
	 * @return Returns true if the item has a display name (that isn't a custom item name)
	 */
	public static boolean hasDisplayName(ItemStack item) {
		if (item == null) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			return false;
		}
		String displayName = meta.getDisplayName();
		if (Strings.isNullOrEmpty(displayName)) {
			return false;
		}
		NamespacedKey customItem = getCustomItemKey(item);
		if (customItem != null) {
			String customName = ChatColor.RESET + ITEM_NAMES.get(customItem);
			return displayName.equals(customName);
		}
		return true;
	}

	private static NamespacedKey getCustomItemKey(ItemStack item) {
		if (item == null) {
			return null;
		}
		NBTCompound nbt = NBTCompound.fromItem(item);
		if (!Validation.checkValidity(nbt) || nbt.isEmpty()) {
			return null;
		}
		String raw = nbt.getString(CUSTOM_ITEM_NBT);
		if (Strings.isNullOrEmpty(raw)) {
			return null;
		}
		return NamespaceAPI.fromString(raw);
	}

}
