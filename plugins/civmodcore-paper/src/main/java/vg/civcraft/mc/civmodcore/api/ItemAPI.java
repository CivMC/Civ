package vg.civcraft.mc.civmodcore.api;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;
import vg.civcraft.mc.civmodcore.events.MaterialNamesLoadEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public final class ItemAPI {
	private ItemAPI() {} // Make the class effectively static

	/**
	 * Checks to see if an item is valid.
	 *
	 * An item is considered invalid if it is null or Air, or if the amount is negative or zero or above the item's maximum stack size.
	 * */
	public static boolean isValidItem(ItemStack item) {
		if (item == null) {
			return false;
		}
		Material material = item.getType();
		if (material == Material.AIR) {
			return false;
		}
		if (item.getAmount() <= 0) {
			return false;
		}
		if (item.getAmount() > item.getMaxStackSize()) {
			return false;
		}
		return true;
	}

	/**
	 * Determines whether two item stacks are functionally identical.
	 *
	 * Two null'd items are not considered equal.
	 * */
	public static boolean areItemsEqual(ItemStack former, ItemStack latter) {
		return former != null && Objects.equals(former, latter);
	}

	/**
	 * Determines whether two item stacks are similar.
	 *
	 * This is very similar to .areItemsEqual(item, item), however this differs in that the item's amount is not considered.
	 * @see ItemAPI#areItemsEqual(ItemStack, ItemStack)
	 *
	 * Two null'd items are not considered equal.
	 * */
	public static boolean areItemsSimilar(ItemStack former, ItemStack latter) {
		if (former == null || latter == null) {
			return false;
		}
		return former.isSimilar(latter);
	}

	/**
	 * Retrieves the ItemMeta from an item.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	@Nonnull
	public static ItemMeta getItemMeta(ItemStack item) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot retrieve item meta; the item is not valid.");
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			throw  new IllegalArgumentException("Cannot retrieve item meta; it has no meta nor was any generated. It's probably an invalid item.");
		}
		return meta;
	}

	/**
	 * Retrieves the display name from an item, which may be null.
	 * It will return null if the display name is not set, or if the display name is empty.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	@Nullable
	public static String getDisplayName(ItemStack item) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot retrieve the display name; the item is not valid.");
		}
		ItemMeta meta = getItemMeta(item);
		String name = meta.getDisplayName();
		if (StringUtils.isEmpty(name)) {
			return null;
		}
		return name;
	}

	/**
	 * Sets a display name to an item.
	 * If the set name is null or empty, it will clear the item's display name.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void setDisplayName(ItemStack item, String name) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot set the display name; the item is not valid.");
		}
		ItemMeta meta = getItemMeta(item);
		if (StringUtils.isNotEmpty(name)) {
			meta.setDisplayName(null);
		}
		else {
			meta.setDisplayName(name);
		}
		item.setItemMeta(meta);
	}

	/**
	 * Retrieves the lore from an item.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	@Nonnull
	public static List<String> getLore(ItemStack item) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot retrieve the item's lore; the item is not valid.");
		}
		List<String> lore = getItemMeta(item).getLore();
		if (lore == null) {
			return new ArrayList<>();
		}
		return lore;
	}

	/**
	 * Sets the lore for an item, clearing our any lore that may have already been set.
	 * This is a programmatically easier version to use as you may define the lore inline, e.g: ItemAPI.setLore(item, "Compacted Item");
	 *
	 * Do not use this to clear lore by setting null lore, instead use clearLore(item)
	 * @see ItemAPI#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void setLore(ItemStack item, String... lines) {
		setLore(item, Arrays.asList(lines));
	}

	/**
	 * Sets the lore for an item, clearing our any lore that may have already been set.
	 *
	 * Do not use this to clear lore by setting null lore, instead use clearLore(item)
	 * @see ItemAPI#clearLore(ItemStack)
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void setLore(ItemStack item, List<String> lines) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot set the item's lore; the item is not valid.");
		}
		if (lines == null) {
			throw new IllegalArgumentException("Cannot set the item's lore; the lore was null. Use ItemAPI.clearLore(item) instead.");
		}
		ItemMeta meta = getItemMeta(item);
		meta.setLore(lines);
		item.setItemMeta(meta);
	}

	/**
	 * Appends lore to an item.
	 * This is a programmatically easier version to use as you may define the lore inline, e.g: ItemAPI.addLore(item, "Compacted Item");
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void addLore(ItemStack item, String... lines) {
		addLore(item, Arrays.asList(lines));
	}

	/**
	 * Appends lore to an item.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void addLore(ItemStack item, List<String> lines) {
		addLore(item, false, lines);
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 * This is a programmatically easier version to use as you may define the lore inline, e.g: ItemAPI.addLore(item, true, "Compacted Item");
	 *
	 * @param prepend If set to true, the lore will be prepended.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void addLore(ItemStack item, boolean prepend, String... lines) {
		addLore(item, prepend, Arrays.asList(lines));
	}

	/**
	 * Adds lore to an item, either by appending or prepending.
	 *
	 * @param prepend If set to true, the lore will be prepended.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void addLore(ItemStack item, boolean prepend, List<String> lines) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot add to the item's lore; the item is not valid.");
		}
		if (lines == null) {
			throw new IllegalArgumentException("Cannot add to the item's lore; the lore was null.");
		}
		List<String> lore = getLore(item);
		if (prepend) {
			Collections.reverse(lines);
			for (String line : lines) {
				lore.add(0, line);
			}
		}
		else {
			lore.addAll(lines);
		}
		setLore(item, lore);
	}

	/**
	 * Clears the lore from an item.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static void clearLore(ItemStack item) {
		if (!isValidItem(item)) {
			throw new IllegalArgumentException("Cannot clear an item's lore; the item is not valid.");
		}
		List<String> lore = getLore(item);
		lore.clear();
		setLore(item, lore);
	}

	// ------------------------------------------------------------
	// Item Names
	// ------------------------------------------------------------

	private static Map<Integer, String> itemNames = new HashMap<>();

	public static void resetItemNames() {
		itemNames.clear();
	}

	public static void loadItemNames() {
		resetItemNames();
		Logger logger = Bukkit.getLogger();
		// Load material names from materials.csv
		InputStream materialsCSV = CivModCorePlugin.class.getResourceAsStream("/materials.csv");
		if (materialsCSV != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(materialsCSV));
				String line = reader.readLine();
				while (line != null) {
					String [] values = line.split(",");
					// If there's not at least three values (slug, data, name) then skip
					if (values.length < 3) {
						logger.warning("This material row does not have enough data: " + line);
						continue;
					}
					// If a material cannot be found by the slug given, skip
					Material material = Material.getMaterial(values[0]);
					if (material == null) {
						logger.warning("Could not find a material on this line: " + line);
						continue;
					}
					// If the name is empty, skip
					String name = values [2];
					if (name.isEmpty()) {
						logger.warning("This material has not been given a name: " + line);
						continue;
					}
					// Put the material, data, and name into the system
					itemNames.put(generateItemHash(material, null), name);
					logger.info(String.format("Material parsed: %s = %s", material, name));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException | NullPointerException e) {
				logger.warning("Could not load materials from materials.csv");
				e.printStackTrace();
			}
		}
		else {
			logger.warning("Could not load materials from materials.csv as the file does not exist.");
		}
		// Load custom material names from config.yml
		// TODO: Add a config parser for material names so that developers may set
		//       item names based on an item's display name and or lore.
		// Allow external plugins to add custom material names programmatically, let them know to do so
		Bukkit.getServer().getPluginManager().callEvent(new LoadCustomItemNamesEvent());
	}

	private static int generateItemHash(Material material, String displayName) {
		int hash = 0;
		if (material != null) {
			hash += material.hashCode();
		}
		if (!StringUtils.isEmpty(displayName)) {
			hash += displayName.hashCode();
		}
		return hash;
	}

	/**
	 * Gets the name of an item based off a material, e.g: POLISHED_GRANITE -> Polished Granite
	 *
	 * @throws IllegalArgumentException If the given material is null.
	 * */
	public static String getItemName(Material material) {
		return getItemName(material, null);
	}

	/**
	 * Gets the name of an item based off of its material, and its display name and lore if it has any.
	 *
	 * @throws IllegalArgumentException If the given item stack is not valid or if the item meta cannot be retrieved or generated.
	 * @see ItemAPI#isValidItem(ItemStack)
	 * */
	public static String getItemName(ItemStack item) {
		if (item == null) {
			return null;
		}
		return getItemName(item.getType(), getDisplayName(item));
	}

	/**
	 * Gets the name of an item based off of its material, and its display name and lore if it has any.
	 *
	 * @throws IllegalArgumentException If the given material is null.
	 * */
	public static String getItemName(Material material, String displayName) {
		return itemNames.get(generateItemHash(material, displayName));
	}

	/**
	 * This event is called after the item names have been loaded from materials.csv and the config.
	 * */
	public static final class LoadCustomItemNamesEvent extends Event {

		private final Logger logger = Bukkit.getLogger();

		/**
		 * Adds a custom item name to an item.
		 *
		 * @param item The item to set the name to.
		 * @param name The name to set to the item.
		 *
		 * @throws IllegalArgumentException If the item is null, or if the name is null or empty.
		 * */
		public void setCustomItemName(ItemStack item, String name) {
			Preconditions.checkNotNull(item, "Cannot set a custom item's name if the item is null.");
			Preconditions.checkNotNull(name, "Cannot set a custom item's name if the name is null.");
			Preconditions.checkArgument(!name.isEmpty(), "Cannot set a custom item's name if the name is empty.");
			String displayName = getDisplayName(item);
			String previousName = itemNames.put(generateItemHash(item.getType(), displayName), name);
			// Log the addition to item names.
			StringBuilder logMessage = new StringBuilder();
			if (displayName == null) {
				logMessage.append(String.format("[%s]", item.getType()));
			}
			else {
				logMessage.append(String.format("[%s \"%s\"]", item.getType(), displayName));
			}
			logMessage.append(" ");
			if (previousName == null) {
				logMessage.append(String.format("was set to: %s", name));
			}
			else {
				logMessage.append(String.format("[%s] was replaced with: %s", previousName, name));
			}
			this.logger.info(logMessage.toString());
		}

		// ------------------------------------------------------------
		// Obligatory for Spigot/Bukkit
		// ------------------------------------------------------------

		private static final HandlerList handlers = new HandlerList();

		@Override @Nonnull
		public HandlerList getHandlers() {
			return handlers;
		}

		public static HandlerList getHandlerList() {
			return handlers;
		}

	}

}
