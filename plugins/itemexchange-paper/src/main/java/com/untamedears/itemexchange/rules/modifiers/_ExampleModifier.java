package com.untamedears.itemexchange.rules.modifiers;

import co.aikar.commands.annotation.CommandAlias;
import com.untamedears.itemexchange.commands.SetCommand;
import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.List;
import javax.annotation.Nonnull;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializationException;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.Validation;

@CommandAlias(SetCommand.ALIAS) // This is needed to make commands work
@Modifier(slug = "EXAMPLE", order = 12345)
public final class _ExampleModifier extends ModifierData {

	// Make sure to have a template instance to make registration easier
	public static final _ExampleModifier TEMPLATE = new _ExampleModifier();

	/**
	 * Constructs a new instance of a modifier.
	 *
	 * @param item The item to base this exchange data on. You can assume that the item has passed a
	 *         {@link ItemUtils#isValidItem(ItemStack)} check.
	 * @return Returns a new instance of the extended class.
	 */
	@Override
	public _ExampleModifier construct(ItemStack item) {
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
	 *         {@link ItemUtils#isValidItem(ItemStack)} check.
	 * @return Returns true if the given item conforms.
	 */
	@Override
	public boolean conforms(ItemStack item) {
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
	public void toNBT(@Nonnull final NBTCompound nbt) {
		throw new NotImplementedException("Please implement me on your class!");
	}

	/**
	 * Deserializes a class into an NBTCompound.
	 *
	 * @param nbt The NBTCompound to deserialize from, which <i>should</i> NEVER be null, so feel free to throw an
	 *         {@link NBTSerializationException} if it is.
	 * @throws NBTSerializationException This is thrown if the implementation has a fatal error deserializing.
	 */
	@Nonnull
	public static NBTSerializable fromNBT(@Nonnull final NBTCompound nbt) {
		final var modifier = new _ExampleModifier();
		// Something here
		return modifier;
	}

	/**
	 * @return Returns a new listing, or null.
	 */
	@Override
	public String getDisplayListing() {
		return null;
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
