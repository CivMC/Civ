package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.custom.items.ItemCriteria;
import vg.civcraft.mc.civmodcore.custom.items.CustomItems;
import vg.civcraft.mc.civmodcore.util.ResourceUtils;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * Class that loads and store item names. Replaces NiceNames.
 */
public final class ItemNames {

	private static final Logger LOGGER = LoggerFactory.getLogger(ItemNames.class.getSimpleName());

	private static final Map<Material, String> MATERIAL_NAMES = new HashMap<>();

	private static final Map<ItemStack, String> NAME_CACHE = new HashMap<>();

	/**
	 * Resets all item names, custom item names included.
	 */
	public static void resetItemNames() {
		MATERIAL_NAMES.clear();
		NAME_CACHE.clear();
	}

	/**
	 * Loads item names from configurable files and requests any custom item names programmatically from plugins.
	 *
	 * @param plugin The CivModCore instance plugin.
	 */
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
			if (MATERIAL_NAMES.containsKey(material)) {
				LOGGER.warn("Material has already been parsed: " + line);
				return;
			}
			// If the name is empty, skip
			String name = TextUtil.parseColor(values[1]);
			if (Strings.isNullOrEmpty(name)) {
				LOGGER.warn("Material has not been given a proper name: " + line);
				return;
			}
			MATERIAL_NAMES.put(material, name);
		})) {
			LOGGER.warn("Could not load materials from materials.csv");
		}
		else {
			LOGGER.info("Loaded a total of " + MATERIAL_NAMES.size() + " item names from materials.csv");
		}
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
		String name = MATERIAL_NAMES.get(material);
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
		item = ItemAPI.normalizeItem(item);
		String cached = NAME_CACHE.get(item);
		if (!Strings.isNullOrEmpty(cached)) {
			return cached;
		}
		ItemCriteria criteria = CustomItems.findMatch(item);
		if (criteria == null) {
			return getItemName(item.getType());
		}
		NAME_CACHE.put(item, cached);
		return cached;
	}

	/**
	 * Determines whether an item has a display name. Use this in lieu of {@link ItemMeta#hasDisplayName()} as this is
	 * is compatible with custom item data, which may be set to the item's display name.
	 *
	 * @param item The item to check.
	 * @return Returns true if the item has a display name (that isn't a custom item name)
	 */
	public static boolean hasDisplayName(ItemStack item) {
		String displayName = ItemAPI.getDisplayName(item);
		if (Strings.isNullOrEmpty(displayName)) {
			return false;
		}
		if (CustomItems.hasNameApplied(item)) {
			return false;
		}
		return true;
	}

}
