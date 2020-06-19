package vg.civcraft.mc.civmodcore.custom.items;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.castOrNull;

import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.NamespaceAPI;

public class ItemCriteria implements ConfigurationSerializable {

	public static final String ID_KEY = "key";

	public static final String NAME_KEY = "name";

	protected final NamespacedKey key;

	protected final String name;

	public ItemCriteria(Map<String, Object> data) {
		this(NamespaceAPI.fromString(castOrNull(String.class, data.get(ID_KEY))),
				castOrNull(String.class, data.get(NAME_KEY)));
	}

	public ItemCriteria(NamespacedKey key, String name) {
		if (key == null) {
			throw new IllegalArgumentException("Cannot create custom item criteria with a null key.");
		}
		if (Strings.isNullOrEmpty(name)) {
			throw new IllegalArgumentException("Cannot create custom item criteria with an invalid name.");
		}
		this.key = key;
		this.name = name;
	}

	public final NamespacedKey getKey() {
		return this.key;
	}

	public final String getName() {
		return this.name;
	}

	public final String getDisplayName() {
		return "" + ChatColor.RESET + this.name;
	}

	public boolean matches(ItemStack item) {
		return false;
	}

	public ItemStack applyToItem(ItemStack item) {
		ItemAPI.setDisplayName(item, getDisplayName());
		return item;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> data = new HashMap<>();
		data.put(ID_KEY, NamespaceAPI.getString(this.key));
		return data;
	}

	public static ItemCriteria deserialize(Map<String, Object> data) {
		return new ItemCriteria(data);
	}

}
