package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.NamespaceAPI;
import vg.civcraft.mc.civmodcore.custom.items.CustomItems;
import vg.civcraft.mc.civmodcore.custom.items.ItemCriteria;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "CUSTOM", order = 0)
public final class CustomItemModifier extends ModifierData {

	public static final CustomItemModifier TEMPLATE = new CustomItemModifier();

	public static final String ID_KEY = "key";

	private NamespacedKey key;

	@Override
	public CustomItemModifier construct(ItemStack item) {
		ItemCriteria criteria = CustomItems.findMatch(item);
		if (criteria == null) {
			return null;
		}
		CustomItemModifier modifier = new CustomItemModifier();
		modifier.key = criteria.getKey();
		return modifier;
	}

	@Override
	public boolean isBroken() {
		if (this.key == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean conforms(ItemStack item) {
		ItemCriteria criteria = CustomItems.findMatch(item);
		if (criteria == null) {
			return false;
		}
		if (!NullCoalescing.equalsNotNull(this.key, criteria.getKey())) {
			return false;
		}
		return true;
	}

	@Override
	public void serialize(NBTCompound nbt) {
		nbt.setString(ID_KEY, NamespaceAPI.getString(this.key));
	}

	@Override
	public void deserialize(NBTCompound nbt) {
		this.key = NamespaceAPI.fromString(nbt.getString(ID_KEY));
	}

	@Override
	public String getDisplayListing() {
		if (this.key == null) {
			return ChatColor.RED + "Unknown Custom Item";
		}
		ItemCriteria criteria = CustomItems.findCriteria(this.key);
		if (criteria == null) {
			return ChatColor.RED + this.key.toString();
		}
		return criteria.getName();
	}

}
