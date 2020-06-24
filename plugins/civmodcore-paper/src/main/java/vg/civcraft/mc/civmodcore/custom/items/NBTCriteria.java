package vg.civcraft.mc.civmodcore.custom.items;

import com.google.common.base.Strings;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.NamespaceAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;
import vg.civcraft.mc.civmodcore.util.Validation;

public class NBTCriteria extends ItemCriteria {

	private static final String CUSTOM_ITEM_NBT = "CustomItem";

	public NBTCriteria(Map<String, Object> data) {
		super(data);
	}

	public NBTCriteria(NamespacedKey key, String name) {
		super(key, name);
	}

	@Override
	public boolean matches(ItemStack item) {
		if (item == null) {
			return false;
		}
		NBTCompound nbt = NBTCompound.fromItem(item);
		if (!Validation.checkValidity(nbt) || nbt.isEmpty()) {
			return false;
		}
		String raw = nbt.getString(CUSTOM_ITEM_NBT);
		if (Strings.isNullOrEmpty(raw)) {
			return false;
		}
		NamespacedKey key = NamespaceAPI.fromString(raw);
		if (!NullCoalescing.equalsNotNull(key, this.key)) {
			return false;
		}
		return true;
	}

	@Override
	public ItemStack applyToItem(ItemStack item) {
		return NBTCompound.processItem(super.applyToItem(item), nbt -> {
			nbt.setString(CUSTOM_ITEM_NBT, this.key.toString());
		});
	}

	public static NBTCriteria deserialize(Map<String, Object> data) {
		return new NBTCriteria(data);
	}

}
