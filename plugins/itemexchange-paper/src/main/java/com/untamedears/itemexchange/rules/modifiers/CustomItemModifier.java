package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.NamespaceAPI;
import vg.civcraft.mc.civmodcore.custom.items.CustomItems;
import vg.civcraft.mc.civmodcore.custom.items.ItemCriteria;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializationException;
import vg.civcraft.mc.civmodcore.util.NullCoalescing;
import vg.civcraft.mc.civmodcore.util.Validation;

@CommandAlias(SetCommand.ALIAS)
@Modifier(slug = "CUSTOM", order = 0)
public class CustomItemModifier extends ModifierData {

	public static final CustomItemModifier TEMPLATE = new CustomItemModifier();

	public static final String ID_KEY = "key";

	private NamespacedKey key;

	/**
	 * Constructs a new instance of a modifier.
	 *
	 * @param item The item to base this exchange data on. You can assume that the item has passed a
	 *         {@link ItemAPI#isValidItem(ItemStack)} check.
	 * @return Returns a new instance of the extended class.
	 */
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

	/**
	 * Essentially a renamed {@link ExchangeData#isValid()} to bring more clarity. isValid is still being used
	 * to maintain usage of {@link Validation#checkValidity(Validation)}. This method is intended to convey
	 * whether this rule is broken, not whether it should be filtered.
	 *
	 * @return Returns true if this rule is broken.
	 */
	@Override
	public boolean isBroken() {
		if (this.key == null) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if an arbitrary item conforms to this exchange data's requirements.
	 *
	 * @param item The arbitrary item to check. You can assume that the item has passed a
	 *         {@link ItemAPI#isValidItem(ItemStack)} check.
	 * @return Returns true if the given item conforms.
	 */
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

	/**
	 * Serializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to serialize into, which <i>should</i> NEVER be null, so feel free to throw an
	 *         {@link NBTSerializationException} if it is. You can generally assume that the nbt compound is new and
	 *         therefore empty, but you may wish to check or manually {@link NBTCompound#clear() empty it}, though the
	 *         latter may cause other issues.
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error serializing.
	 */
	@Override
	public void serialize(NBTCompound nbt) {
		nbt.setString(ID_KEY, NamespaceAPI.getString(this.key));
	}

	/**
	 * Deserializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to deserialize from, which <i>should</i> NEVER be null, so feel free to throw an
	 *         {@link NBTSerializationException} if it is.
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error deserializing.
	 */
	@Override
	public void deserialize(NBTCompound nbt) {
		this.key = NamespaceAPI.fromString(nbt.getString(ID_KEY));
	}

	/**
	 * @return Returns a new listing, or null.
	 */
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

	/**
	 * @return Returns a set of strings to be displayed as part of {@link ExchangeRule}'s details. Note: Null or
	 *     empty lists are supported and convey to not list anything.
	 */
	@Override
	public List<String> getDisplayInfo() {
		return null;
	}

}
