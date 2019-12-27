package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Preconditions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.CivModCorePlugin;

/**
 * Class that loads and store item names. Replaces NiceNames.
 * */
public final class ItemNames {

	private static final Logger logger = Bukkit.getLogger();

	private static final Map<Integer, String> itemNames = new HashMap<>();

	private ItemNames() { } // Make the class effectively static

	/**
	 * Resets all item names, custom item names included.
	 * */
	public static void resetItemNames() {
		itemNames.clear();
	}

	/**
	 * Loads item names from configurable files and requests any custom item names programmatically from plugins.
	 * */
	public static void loadItemNames() {
		resetItemNames();
		// Load material names from materials.csv
		InputStream materialsCSV = CivModCorePlugin.class.getResourceAsStream("/materials.csv");
		if (materialsCSV != null) {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(materialsCSV));
				String line = reader.readLine();
				while (line != null) {
					String [] values = line.split(",");
					// If there's not at least three values (slug, data, name) then skip
					if (values.length < 2) {
						logger.warning("This material row does not have enough data: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If a material cannot be found by the slug given, skip
					Material material = Material.getMaterial(values[0]);
					if (material == null) {
						logger.warning("Could not find a material on this line: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// If the name is empty, skip
					String name = values[1];
					if (name.isEmpty()) {
						logger.warning("This material has not been given a name: " + line);
						// Go to the next line
						line = reader.readLine();
						continue;
					}
					// Put the material and name into the system
					itemNames.put(generateItemHash(material, null), name);
					logger.info(String.format("Material parsed: %s = %s", material, name));
					line = reader.readLine();
				}
				reader.close();
			}
			catch (IOException error) {
				logger.log(Level.WARNING, "Could not load materials from materials.csv", error);
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

	private static int generateItemHash(@Nullable Material material, @Nullable String displayName) {
		int hash = 0;
		if (material != null) {
			hash += material.hashCode();
		}
		if (StringUtils.isNotEmpty(displayName)) {
			hash += displayName.hashCode();
		}
		return hash;
	}

	/**
	 * Gets the name of an item based off a material, e.g: POLISHED_GRANITE to Polished Granite
	 *
	 * @param material The material to get the name of.
	 * @return Returns the material name, or null is none is set.
	 *
	 * @throws IllegalArgumentException If the given material is null.
	 * */
	@Nullable
	public static String getItemName(@Nonnull Material material) {
		Preconditions.checkNotNull(material, "Cannot retrieve the material's name; the material is null.");
		return itemNames.get(generateItemHash(material, null));
	}

	/**
	 * Gets the name of an item based off of its material, and its display name if it has one.
	 *
	 * @param item The item to get the name of.
	 * @return Returns the item's name, or null is none is set.
	 * */
	@Nullable
	public static String getItemName(@Nonnull ItemStack item) {
		Preconditions.checkNotNull(item, "Cannot retrieve the item's name; the item is null.");
		return itemNames.get(generateItemHash(item.getType(), ItemAPI.getDisplayName(item)));
	}

	/**
	 * This event is called after the item names have been loaded from materials.csv and the config.
	 * */
	public static final class LoadCustomItemNamesEvent extends Event {

		private static final HandlerList handlers = new HandlerList();

		/**
		 * Adds a custom item name to an item.
		 *
		 * @param item The item to set the name to.
		 * @param name The name to set to the item.
		 *
		 * @throws IllegalArgumentException If the item is null, or if the name is null or empty.
		 * */
		public void setCustomItemName(@Nonnull ItemStack item, @Nonnull String name) {
			Preconditions.checkNotNull(item, "Cannot set a custom item's name if the item is null.");
			Preconditions.checkNotNull(name, "Cannot set a custom item's name if the name is null.");
			Preconditions.checkArgument(!name.isEmpty(), "Cannot set a custom item's name if the name is empty.");
			String displayName = ItemAPI.getDisplayName(item);
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
			logger.info(logMessage.toString());
		}

		/**
		 * Get all this event's handlers.
		 *
		 * @return Returns this event's handlers.
		 * */
		@Override
		@Nonnull
		public HandlerList getHandlers() {
			return handlers;
		}

		/**
		 * Statically get all this event's handlers.
		 *
		 * @return Returns this event's handlers.
		 * */
		@Nonnull
		public static HandlerList getHandlerList() {
			return handlers;
		}

	}

}
