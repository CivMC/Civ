package com.github.igotyou.FactoryMod.recipes;

/**
 * Wrapper for current and all future enchantment control options.
 * 
 * @author ProgrammerDan
 * @since v1.4.0
 * 
 */
public class EnchantmentOptions {
	private boolean safeOnly;
	private boolean ensureOne;
	
	public static final EnchantmentOptions DEFAULT = new EnchantmentOptions(false, false);
	
	/**
	 * Standard constructor, sets the flag values.
	 * 
	 * @param safeOnly true if only MC combinable enchantments allowed
	 * @param ensureOne true if should ensure at least one enchantment.
	 */
	public EnchantmentOptions(boolean safeOnly, boolean ensureOne) {
		this.safeOnly = safeOnly;
		this.ensureOne = ensureOne;
	}
	
	/**
	 * Returns true if only safe to combine enchantments should be allowed.
	 * 
	 * @return the value of the control flag.
	 */
	public boolean getSafeOnly() {
		return this.safeOnly;
	}
	
	/**
	 * Return true if only mod should ensure at least one enchantment on output.
	 * @return
	 */
	public boolean getEnsureOne() {
		return this.ensureOne;
	}
}
