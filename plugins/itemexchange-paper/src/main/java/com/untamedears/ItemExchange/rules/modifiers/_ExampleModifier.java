package com.untamedears.itemexchange.rules.modifiers;

import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.List;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializationException;
import vg.civcraft.mc.civmodcore.util.Validation;

@Modifier(slug = "EXAMPLE", order = 12345)
public class _ExampleModifier extends ModifierData<_ExampleModifier> {

	/**
	 * Constructs a new instance of the modifier.
	 *
	 * @return Returns a new instance of the extended class.
	 */
	@Override
	public _ExampleModifier construct() {
		return null;
	}

	/**
	 * Constructs a new instance of a modifier.
	 *
	 * @param item The item to base this exchange data on. You can assume that the item has passed a
	 *         {@link ItemAPI#isValidItem(ItemStack)} check.
	 * @return Returns a new instance of the extended class.
	 */
	@Override
	public _ExampleModifier construct(@NotNull ItemStack item) {
		return null;
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
	public boolean conforms(@NotNull ItemStack item) {
		return false;
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

	}

	/**
	 * @return Returns a new listing, or null.
	 */
	@Override
	public String getDisplayedListing() {
		return null;
	}

	/**
	 * @return Returns a set of strings to be displayed as part of {@link ExchangeRule}'s details. Note: Null or
	 *     empty lists are supported and convey to not list anything.
	 */
	@Override
	public List<String> getDisplayedInfo() {
		return null;
	}

}
