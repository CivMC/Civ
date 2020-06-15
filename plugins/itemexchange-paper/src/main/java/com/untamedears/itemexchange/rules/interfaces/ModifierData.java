package com.untamedears.itemexchange.rules.interfaces;

import com.untamedears.itemexchange.rules.ExchangeRule;
import java.util.List;
import java.util.Objects;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.command.AikarCommand;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;

/**
 * Abstract class that represents a modifier.
 *
 * @param <T> The type of the child class, for example:
 * {@code public final class BookModifier extends ModifierData<BookModifier> {}}
 */
public abstract class ModifierData<T extends ModifierData<T>> extends AikarCommand
		implements ExchangeData, NBTSerializable, Comparable<ModifierData<?>> {

	private static final int hashOffset = 37513459;

	private final Modifier modifier;

	/**
	 * Constructs a new Modifier. This should <b>ONLY</b> be called by a implementor's default constructor. The
	 * given parameters should only change between modifiers, not between states of the same modifier.
	 */
	protected ModifierData() {
		this.modifier = getClass().getAnnotation(Modifier.class);
		if (this.modifier == null) {
			throw new IllegalStateException("Modifiers MUST have the @Modifier annotation.");
		}
	}

	/**
	 * @return Returns the identifier for this modifier.
	 */
	public final String getSlug() {
		return this.modifier.slug().toUpperCase();
	}

	/**
	 * Constructs a new instance of the modifier.
	 *
	 * @return Returns a new instance of the extended class.
	 */
	public abstract T construct();

	/**
	 * Constructs a new instance of a modifier.
	 *
	 * @param item The item to base this exchange data on. You can assume that the item has passed a
	 *     {@link vg.civcraft.mc.civmodcore.api.ItemAPI#isValidItem(ItemStack)} check.
	 * @return Returns a new instance of the extended class.
	 */
	public abstract T construct(ItemStack item);

	/**
	 * Duplicates this modifier.
	 */
	public final T duplicate() {
		NBTCompound nbt = new NBTCompound();
		serialize(nbt);
		T instance = construct();
		instance.deserialize(nbt);
		return instance;
	}

	/**
	 * Checks if an arbitrary item conforms to this exchange data's requirements.
	 *
	 * @param item The arbitrary item to check. You can assume that the item has passed a
	 *     {@link vg.civcraft.mc.civmodcore.api.ItemAPI#isValidItem(ItemStack)} check.
	 * @return Returns true if the given item conforms.
	 */
	public abstract boolean conforms(ItemStack item);

	/**
	 * @return Returns a new listing, or null.
	 */
	public String getDisplayedListing() {
		return null;
	}

	/**
	 * @return Returns a set of strings to be displayed as part of {@link ExchangeRule}'s details. Null or empty lists
	 *     are supported and convey to not list anything.
	 */
	@Override
	public List<String> getDisplayedInfo() {
		return null;
	}

	@Override
	public final int compareTo(ModifierData other) {
		return Integer.compare(this.modifier.order(), other.modifier.order());
	}

	@Override
	public final int hashCode() {
		return hashOffset + Objects.hash(this.modifier.slug());
	}

	@Override
	public final boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ModifierData)) {
			return false;
		}
		if (hashCode() != other.hashCode()) {
			return false;
		}
		return true;
	}

}
