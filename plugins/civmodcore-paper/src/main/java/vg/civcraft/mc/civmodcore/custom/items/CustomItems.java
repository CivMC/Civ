package vg.civcraft.mc.civmodcore.custom.items;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

/**
 *
 */
public final class CustomItems {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomItems.class.getSimpleName());

	private static final Map<NamespacedKey, ItemCriteria> CUSTOM_ITEMS = new HashMap<>();

	public static ItemCriteria register(ItemCriteria criteria) {
		if (criteria == null) {
			throw new IllegalArgumentException("Cannot register a non-existent custom item criteria.");
		}
		if (CUSTOM_ITEMS.containsKey(criteria.key)) {
			throw new IllegalArgumentException("Cannot register a custom item to a key that already exists: " +
					criteria.getKey());
		}
		CUSTOM_ITEMS.put(criteria.key, criteria);
		LOGGER.info("Registered: " + criteria.key);
		return criteria;
	}

	public static void deregister(NamespacedKey key) {
		if (key == null || !CUSTOM_ITEMS.containsKey(key)) {
			return;
		}
		CUSTOM_ITEMS.remove(key);
		LOGGER.info("De-registered: " + key);
	}

	public static void clearRegistrations() {
		CUSTOM_ITEMS.clear();
	}

	public static ItemCriteria findCriteria(NamespacedKey key) {
		if (key == null) {
			return null;
		}
		return CUSTOM_ITEMS.get(key);
	}

	public static ItemCriteria findMatch(ItemStack item) {
		if (item == null) {
			return null;
		}
		return CUSTOM_ITEMS.values().stream()
				.filter(criteria -> criteria.matches(item))
				.findFirst().orElse(null);
	}

	public static boolean hasNameApplied(ItemStack item) {
		String displayName = ItemAPI.getDisplayName(item);
		if (Strings.isNullOrEmpty(displayName)) {
			return false;
		}
		ItemCriteria criteria = findMatch(item);
		if (criteria == null) {
			return false;
		}
		String customName = "" + ChatColor.RESET + criteria.getName();
		if (!NullCoalescing.equalsNotNull(customName, displayName)) {
			return false;
		}
		return true;
	}

}
