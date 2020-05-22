package vg.civcraft.mc.civmodcore.serialization.structs;

import org.bukkit.enchantments.Enchantment;
import vg.civcraft.mc.civmodcore.api.EnchantAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCache;

/**
 * NBT Compound data class for an Enchantment & Level pair.
 */
public final class LeveledEnchantNBT extends NBTCache {

	public static final String ENCHANT_KEY = "enchant";

	public static final String LEVEL_KEY = "level";

	public LeveledEnchantNBT() { }

	/**
	 * Construct an Enchantment & Level pair with preset values.
	 */
	public LeveledEnchantNBT(Enchantment enchantment, int level) {
		setEnchant(enchantment);
		setLevel(level);
	}

	/**
	 * Retrieves a matching enchantment from the underlying NBT.
	 *
	 * @return Returns a matched enchantment, or null.
	 */
	public Enchantment getEnchant() {
		return EnchantAPI.getEnchantment(this.nbt.getString(ENCHANT_KEY));
	}

	/**
	 * Sets an enchantment to the underlying NBT.
	 *
	 * @param enchant The enchantment to set.
	 */
	@SuppressWarnings("deprecation")
	public void setEnchant(Enchantment enchant) {
		this.nbt.setString(ENCHANT_KEY, enchant == null ? null : enchant.getName());
	}

	/**
	 * Retrieves the level set on the underlying NBT.
	 *
	 * @return Returns the set level, or default: 0
	 */
	public int getLevel() {
		return this.nbt.getInteger(LEVEL_KEY);
	}

	/**
	 * Sets a level onto the underlying NBT.
	 *
	 * @param level The level to set.
	 */
	public void setLevel(int level) {
		this.nbt.setInteger(LEVEL_KEY, level);
	}

	@Override
	public int hashCode() {
		return 873524 + this.nbt.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof LeveledEnchantNBT)) {
			return false;
		}
		return this.nbt.equals(((LeveledEnchantNBT) object).nbt);
	}

}
