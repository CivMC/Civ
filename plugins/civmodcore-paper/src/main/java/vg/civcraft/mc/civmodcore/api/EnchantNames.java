package vg.civcraft.mc.civmodcore.api;

import com.google.common.base.Strings;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;
import vg.civcraft.mc.civmodcore.inventory.items.EnchantUtils;

/**
 * @deprecated Use {@link EnchantUtils} instead.
 */
@Deprecated
public final class EnchantNames {

	@Deprecated
	public static SearchResult findByEnchantment(Enchantment enchant) {
		if (enchant == null) {
			return null;
		}
		return new SearchResult(enchant,
				EnchantUtils.getEnchantAbbreviation(enchant),
				EnchantUtils.getEnchantNiceName(enchant));
	}

	@Deprecated
	public static SearchResult findByAbbreviation(String abbreviation) {
		if (Strings.isNullOrEmpty(abbreviation)) {
			return null;
		}
		final Enchantment found = EnchantUtils.getEnchantment(abbreviation);
		if (found == null) {
			return null;
		}
		return new SearchResult(found, abbreviation, EnchantUtils.getEnchantNiceName(found));
	}

	@Deprecated
	public static SearchResult findByDisplayName(String displayName) {
		if (Strings.isNullOrEmpty(displayName)) {
			return null;
		}
		final Enchantment found = EnchantUtils.getEnchantment(displayName);
		if (found == null) {
			return null;
		}
		return new SearchResult(found, EnchantUtils.getEnchantAbbreviation(found), displayName);
	}

	/**
	 * This class represents a data set for a particular enchantment.
	 */
	@Deprecated
	public static final class SearchResult {

		private final Enchantment enchantment;

		private final String abbreviation;

		private final String displayName;

		private SearchResult(Enchantment enchantment, String abbreviation, String displayName) {
			this.enchantment = enchantment;
			this.abbreviation = abbreviation;
			this.displayName = displayName;
		}

		/**
		 * @return Returns the enchantment itself.
		 */
		public Enchantment getEnchantment() {
			return this.enchantment;
		}

		/**
		 * @return Returns the enchantment's official abbreviation.
		 */
		public String getAbbreviation() {
			return this.abbreviation;
		}

		/**
		 * @return Returns the enchantment's official display name.
		 */
		public String getDisplayName() {
			return this.displayName;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.enchantment, this.displayName, this.abbreviation);
		}

	}

}
