package com.untamedears.itemexchange.rules.interfaces;

import java.util.List;
import vg.civcraft.mc.civmodcore.nbt.NBTSerializable;
import vg.civcraft.mc.civmodcore.utilities.Validation;

/**
 * This class forms the basis of all exchange data.
 */
public interface ExchangeData extends Validation, NBTSerializable {

	/**
	 * Generate data to be displayed, which will be displayed in chat <i>AND</i> on items as law. Ensure you format
	 * correctly.
	 *
	 * @return Returns any display information.
	 */
	List<String> getDisplayInfo();

	/**
	 * Use {@link ExchangeData#isBroken()} instead.
	 *
	 * @deprecated Do not override this method.
	 */
	@Override
	@Deprecated
	default boolean isValid() {
		return !isBroken();
	}

	/**
	 * Essentially a renamed {@link ExchangeData#isValid()} to bring more clarity. isValid is still being used
	 * to maintain usage of {@link Validation#checkValidity(Validation)}. This method is intended to convey
	 * whether this rule is broken, not whether it should be filtered.
	 *
	 * @return Returns true if this rule is broken.
	 */
	boolean isBroken();

}
