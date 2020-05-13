package com.untamedears.itemexchange.rules;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;
import com.google.common.base.Strings;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.api.EnchantNames;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;
import vg.civcraft.mc.civmodcore.util.CMCLogger;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.TextUtil;

/**
 * This class represents an exchange rule.
 */
public final class ExchangeRule extends ExchangeData {

	/**
	 * The type of rule.
	 */
	public enum Type {
		INPUT, OUTPUT, BROKEN
	}

	// ------------------------------------------------------------
	//
	// ------------------------------------------------------------

	public static final short NEW = 0;

	public static final short ANY = -1;

	public static final short USED = -2;

	public static final short ERROR = -99;

	// ------------------------------------------------------------
	// NBT Keys
	// ------------------------------------------------------------

	private static final String RULE_KEY = "ExchangeRule";

	private static final String TYPE_KEY = "type";

	private static final String MATERIAL_KEY = "material";

	private static final String AMOUNT_KEY = "amount";

	private static final String REQUIRED_ENCHANTS_KEY = "requiredEnchants";

	private static final String EXCLUDED_ENCHANTS_KEY = "excludedEnchants";

	private static final String ALLOW_UNLISTED_ENCHANTS_KEY = "allowingUnlistedEnchants";

	private static final String DISPLAY_NAME_KEY = "displayName";

	private static final String IGNORE_DISPLAY_NAME_KEY = "ignoringDisplayName";

	private static final String LORE_KEY = "lore";

	private static final String GROUP_KEY = "group";

	private static final String MODIFIERS_KEY = "modifiers";

	private static final String ENCHANT_KEY = "enchant";

	private static final String ENCHANT_LEVEL_KEY = "level";

	// ------------------------------------------------------------
	// Cached values - Make sure to [re/un]set these on value set
	// ------------------------------------------------------------

	private List<ModifierData<?>> modifiersCache = null;

	// ------------------------------------------------------------
	//
	// ------------------------------------------------------------

	private final CMCLogger logger = new CMCLogger(ItemExchangePlugin.class);

	/**
	 * Creates a third generation Exchange rule.
	 */
	public ExchangeRule() {
		this.nbt.setInteger("version", 3);
	}

	/**
	 * Base the exchange rule on the given item.
	 *
	 * @param type lol
	 * @param item The item to base this exchange data on.
	 */
	public ExchangeRule(Type type, ItemStack item) {
		setType(type);
		setMaterial(item.getType());
		setAmount(item.getAmount());
		ItemAPI.handleItemMeta(item, (ItemMeta meta) -> {
			if (meta.hasDisplayName()) {
				setDisplayName(meta.getDisplayName());
			}
			if (meta.hasLore()) {
				setLore(meta.getLore());
			}
			if (meta.hasEnchants()) {
				setRequiredEnchants(meta.getEnchants());
			}
			return false;
		});
		addModifiers(ModifierRegistrar.getModifiers()
				.map((modifier) -> modifier.construct(item))
				.filter(Objects::nonNull)
				.toArray(ModifierData<?>[]::new));
	}

	@Override
	public boolean isValid() {
		if (!Iteration.contains(getType(), Type.INPUT, Type.OUTPUT)) {
			return false;
		}
		if (!MaterialAPI.isValidItemMaterial(getMaterial())) {
			return false;
		}
		if (getAmount() < 1) {
			return false;
		}
		return true;
	}

	/**
	 * Checks if an arbitrary item conforms to this exchange data's requirements.
	 *
	 * @param item The arbitrary item to check.
	 * @return Returns true if the given item conforms.
	 */
	public boolean conforms(ItemStack item) {
		Material material = getMaterial();
		if (!Objects.equals(material, item.getType())) {
			this.logger.debug("[ExchangeRule] Material does not match.");
			return false;
		}
		if (item.getAmount() <= 0) {
			this.logger.debug("[ExchangeRule] Item doesn't have an amount.");
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			this.logger.debug("[ExchangeRule] No ItemMeta.");
			return false;
		}
		if (!Utilities.conformsRequiresEnchants(getRequiredEnchants(), meta.getEnchants(),
				isAllowingUnlistedEnchants())) {
			this.logger.debug("[ExchangeRule] Enchantments do not match.");
			return false;
		}
		Set<Enchantment> exclEnchants = getExcludedEnchants();
		if (!exclEnchants.isEmpty()) {
			if (!Collections.disjoint(meta.getEnchants().keySet(), exclEnchants)) {
				this.logger.debug("[ExchangeRule] Item has excluded enchantments.");
				return false;
			}
		}
		if (!isIgnoringDisplayName()) {
			String ruleDisplayName = getDisplayName();
			boolean hasRuleDisplayName = ruleDisplayName != null;
			if (!hasRuleDisplayName && meta.hasDisplayName()) {
				return false;
			}
			else if (hasRuleDisplayName && !meta.hasDisplayName()) {
				return false;
			}
			else if (hasRuleDisplayName && meta.hasDisplayName()) {
				if (!Objects.equals(meta.getDisplayName(), getDisplayName())) {
					this.logger.debug("[ExchangeRule] Display name doesn't match.");
					return false;
				}
			}
		}
		if (!Objects.equals(meta.hasLore() ? meta.getLore() : Collections.emptyList(), getLore())) {
			this.logger.debug("[ExchangeRule] Lore not equal.");
			return false;
		}
		for (ModifierData<?> modifier : getModifiers()) {
			if (!modifier.conforms(item)) {
				this.logger.debug(String.format("[ExchangeRule] [%s] Modifier rejected that.",
						modifier.getClass().getSimpleName()));
				return false;
			}
		}
		// DO NOT MATCH GROUPS HERE, THIS IS CHECKED IN ItemExchangeListener!
		return true;
	}

	/**
	 * Gets a listing for this rule. If none is set then one is automatically generated.
	 *
	 * @return Returns the rule's listing.
	 */
	private String getListing() {
		String name = ItemNames.getItemName(getMaterial());
		String displayName = getDisplayName();
		if (!Strings.isNullOrEmpty(displayName)) {
			name += " " + ChatColor.WHITE + ChatColor.ITALIC + "\"" + displayName + ChatColor.WHITE +
					ChatColor.ITALIC + "\"";
		}
		return niceName;
	}

	/**
	 * Gets the details title of this exchange rule.
	 *
	 * @return Return this exchange rule's details title.
	 *
	 * @apiNote This is essentially the first line of {@link ExchangeRule#getRuleDetails()} but needs to be separate
	 *     so that when creating a rule item, the item's display name is set to the title, and the remainder of the
	 *     details is set to the item's lore.
	 */
	private String getRuleTitle() {
		String title = "" + ChatColor.YELLOW;
		switch (getType()) {
			case INPUT:
				title += "Input";
				break;
			case OUTPUT:
				title += "Output";
				break;
			default:
				title += "Broken";
				break;
		}
		return title + ": " + ChatColor.WHITE + getAmount() + " " + getListing();
	}

	private List<String> getRuleDetails() {
		List<String> info = new ArrayList<>();
		if (isIgnoringDisplayName()) {
			info.add(ChatColor.GOLD + "Ignoring display name");
		}
		if (ItemExchangePlugin.CAN_ENCHANT.contains(getMaterial())) {
			for (Map.Entry<Enchantment, Integer> requiredEnchant : getRequiredEnchants().entrySet()) {
				if (requiredEnchant.getValue() == ANY) {
					info.add(ChatColor.AQUA + EnchantNames.findByEnchantment(requiredEnchant.getKey()).getDisplayName());
				}
				else {
					info.add(ChatColor.AQUA + EnchantNames.findByEnchantment(requiredEnchant.getKey()).getDisplayName() + " " + requiredEnchant.getValue());
				}
			}
			for (Enchantment excludedEnchant : getExcludedEnchants()) {
				info.add(ChatColor.RED + "!" + EnchantNames.findByEnchantment(excludedEnchant).getDisplayName());
			}
			if (isAllowingUnlistedEnchants()) {
				info.add(ChatColor.GREEN + "Other enchantments allowed");
			}
		}
		for (String line : getLore()) {
			if (!Strings.isNullOrEmpty(line)) {
				info.add("" + ChatColor.DARK_PURPLE + ChatColor.ITALIC + line);
			}
		}
		for (ModifierData<?> modifier : getModifiers()) {
			List<String> lines = modifier.getDisplayedInfo();
			if (lines == null || lines.isEmpty()) {
				continue;
			}
			info.addAll(lines);
		}
		String groupName = getGroupName();
		if (!Strings.isNullOrEmpty(groupName)) {
			info.add(ChatColor.RED + "Restricted to " + groupName);
		}
		return info;
	}

	@Override
	public List<String> getDisplayedInfo() {
		List<String> info = new ArrayList<>();
		info.add(getRuleTitle());
		info.addAll(getRuleDetails());
		return info;
	}

	// ------------------------------------------------------------
	//
	// ------------------------------------------------------------

	/**
	 * Gets the type of this rule.
	 *
	 * @return Returns the type of this rule, which will never be null. Anything that doesn't fit into the
	 *     INPUT/OUTPUT dichotomy will be represented as BROKEN.
	 */
	public Type getType() {
		String type = this.nbt.getString(TYPE_KEY);
		if ("INPUT".equalsIgnoreCase(type)) {
			return Type.INPUT;
		}
		if ("OUTPUT".equalsIgnoreCase(type)) {
			return Type.OUTPUT;
		}
		return Type.BROKEN;
	}

	/**
	 * Sets the type of this rule.
	 *
	 * @param type The type to set.
	 */
	public void setType(Type type) {
		if (type == null) {
			this.nbt.remove(TYPE_KEY);
		}
		else {
			this.nbt.setString(TYPE_KEY, type.name());
		}
	}

	/**
	 * Switches an input rule to an output, and vice versa.
	 */
	public void switchIO() {
		switch (getType()) {
			case INPUT:
				setType(Type.OUTPUT);
				break;
			case OUTPUT:
				setType(Type.INPUT);
				break;
			default:
				break;
		}
	}

	/**
	 * Gets the material of this rule.
	 *
	 * @return Returns this rule's listed material.
	 */
	public Material getMaterial() {
		Material material = MaterialAPI.getMaterial(this.nbt.getString(MATERIAL_KEY));
		if (material == null) {
			return Material.AIR;
		}
		return material;
	}

	/**
	 * Sets the material of this rule.
	 *
	 * @param material The material to set to this rule.
	 */
	public void setMaterial(Material material) {
		if (material == null) {
			this.nbt.remove(MATERIAL_KEY);
		}
		else {
			this.nbt.setString(MATERIAL_KEY, material.name());
		}
	}

	/**
	 * Gets the rule's amount.
	 *
	 * @return Return this rule's listed amount.
	 */
	public int getAmount() {
		return this.nbt.getInteger(AMOUNT_KEY);
	}

	/**
	 * Sets the rule's amount.
	 *
	 * @param amount The amount to set to this rule.
	 */
	public void setAmount(int amount) {
		this.nbt.setInteger(AMOUNT_KEY, amount);
	}

	/**
	 * Gets the enchantments listed on this rule as required.
	 *
	 * @return Returns a map of required enchantments and their levels.
	 */
	@SuppressWarnings("deprecation")
	public Map<Enchantment, Integer> getRequiredEnchants() {
		return Arrays.stream(this.nbt.getCompoundArray(REQUIRED_ENCHANTS_KEY))
				.collect(Collectors.toMap(
						(nbt) -> Enchantment.getByName(nbt.getString(ENCHANT_KEY)),
						(nbt) -> nbt.getInteger(ENCHANT_LEVEL_KEY)));
	}

	/**
	 * Sets a map of required enchantments and their levels.
	 *
	 * @param requiredEnchants The map of required enchantments and their respective levels.
	 */
	@SuppressWarnings("deprecation")
	public void setRequiredEnchants(Map<Enchantment, Integer> requiredEnchants) {
		this.nbt.setCompoundArray(REQUIRED_ENCHANTS_KEY, chain(() -> requiredEnchants.entrySet().stream()
				.map(entry -> new NBTCompound() {{
						setString(ENCHANT_KEY, chain(() -> entry.getKey().getName()));
						setInteger(ENCHANT_LEVEL_KEY, entry.getValue());
					}})
				.toArray(NBTCompound[]::new)));
	}

	/**
	 * Gets the enchantments listed on this rule as excluded.
	 *
	 * @return Returns a set of excluded enchantments.
	 */
	@SuppressWarnings("deprecation")
	public Set<Enchantment> getExcludedEnchants() {
		return Arrays.stream(nbt.getStringArray(EXCLUDED_ENCHANTS_KEY))
				.map(Enchantment::getByName)
				.collect(Collectors.toSet());
	}

	/**
	 * Sets a set of excluded enchantments.
	 *
	 * @param excludedEnchants The set of excluded enchantments.
	 */
	@SuppressWarnings("deprecation")
	public void setExcludedEnchants(Set<Enchantment> excludedEnchants) {
		this.nbt.setStringArray(EXCLUDED_ENCHANTS_KEY, chain(() -> excludedEnchants.stream()
				.map(entry -> chain(entry::getName))
				.toArray(String[]::new)));
	}

	/**
	 * Determines whether unlisted enchantments are allowed during the conformation check.
	 *
	 * @return Returns true if unlisted enchantments are allowed.
	 */
	public boolean isAllowingUnlistedEnchants() {
		return this.nbt.getBoolean(ALLOW_UNLISTED_ENCHANTS_KEY);
	}

	/**
	 * Sets whether unlisted enchantments are allowed during the conformation check.
	 *
	 * @param allowingUnlistedEnchants Set this to true if you want to allow unlisted enchantments.
	 */
	public void setAllowingUnlistedEnchants(boolean allowingUnlistedEnchants) {
		this.nbt.setBoolean(ALLOW_UNLISTED_ENCHANTS_KEY, allowingUnlistedEnchants);
	}

	/**
	 * Gets the listed display name.
	 *
	 * @return Returns the listed display name.
	 *
	 * @apiNote Any display name that returns true with {@link Strings#isNullOrEmpty(String)} should be
	 *     interpreted as not existing; that the item does not have a display name.
	 */
	public String getDisplayName() {
		return this.nbt.getString(DISPLAY_NAME_KEY);
	}

	/**
	 * Sets the display name for this rule.
	 *
	 * @param displayName The display name to set. A null or empty string will clear the display name.
	 */
	public void setDisplayName(String displayName) {
		this.nbt.setString(DISPLAY_NAME_KEY, displayName);
	}

	/**
	 * Determines whether an item's display name should be disregarded in conformity checks.
	 *
	 * @return Returns true if an item's display name should be disregarded in conformity checks.
	 */
	public boolean isIgnoringDisplayName() {
		return this.nbt.getBoolean(IGNORE_DISPLAY_NAME_KEY);
	}

	/**
	 * Sets whether an item's display name should be regarded or not during conformity checks.
	 *
	 * @param ignoringDisplayName Set this to true if you want conformity checks to disregard display names.
	 */
	public void setIgnoringDisplayName(boolean ignoringDisplayName) {
		if (ignoringDisplayName) {
			this.nbt.setBoolean(IGNORE_DISPLAY_NAME_KEY, true);
		}
		else {
			this.nbt.remove(IGNORE_DISPLAY_NAME_KEY);
		}
	}

	/**
	 * Gets the listed lore.
	 *
	 * @return Returns the rule's listed lore in its entirety.
	 */
	public List<String> getLore() {
		return Arrays.asList(this.nbt.getStringArray(LORE_KEY));
	}

	/**
	 * Sets the listing's lore.
	 *
	 * @param lore The lore to set in its entirety.
	 */
	public void setLore(List<String> lore) {
		this.nbt.setStringArray(LORE_KEY, lore == null ? null : lore.toArray(new String[0]));
	}

	/**
	 * Gets the name of the group this rule is restricted to.
	 *
	 * @return The name of the group this rule is restricted to. A null or empty string means no restriction.
	 */
	public String getGroupName() {
		return this.nbt.getString(GROUP_KEY);
	}

	/**
	 * Sets the name of the group to restrict this rule to.
	 *
	 * @param group The name of the group to restrict this rule to. A null or empty string clears the
	 *     group restriction.
	 */
	public void setGroupName(String group) {
		this.nbt.setString(GROUP_KEY, group);
	}

	private Stream<? extends ModifierData<?>> getRawModifiers() {





		return Arrays.stream(this.nbt.getCompoundArray(MODIFIERS_KEY))
				.map((nbt) -> chain(() -> (ModifierData<?>) NBTSerialization.deserialize(nbt)))
				.filter(Objects::nonNull);
	}

	/**
	 * Gets all the modifiers on this rule.
	 *
	 * @return Returns an ordered array of the modifiers on this rule.
	 */
	public ModifierData<?>[] getModifiers() {
		if (this.modifiersCache == null) {
			this.modifiersCache = Arrays.stream(this.nbt.getCompoundArray(MODIFIERS_KEY))
					.map((nbt) -> chain(() -> (ModifierData<?>) NBTSerialization.deserialize(nbt)))
					.filter(Objects::nonNull)
					.distinct()
					.sorted()
					.collect(Collectors.toCollection(ArrayList::new));
		}


		return getRawModifiers().sorted().toArray(ModifierData<?>[]::new);
	}

	/**
	 * Handles the modifiers on this rule.
	 *
	 * @param handler The handler for this rule.
	 *
	 * @apiNote The modifiers given to the handler will not be sorted. If your handler makes any changes that you wish
	 *     to save to the rule, then set your handler to return true.
	 */
	public void handleModifiers(Predicate<Set<ModifierData<?>>> handler) {
		if (handler == null) {
			return;
		}
		Set<ModifierData<?>> modifiers = getRawModifiers().collect(Collectors.toCollection(HashSet::new));
		if (handler.test(modifiers)) {
			if (Iteration.isNullOrEmpty(modifiers)) {
				this.nbt.remove(MODIFIERS_KEY);
				return;
			}
			this.nbt.setCompoundArray(MODIFIERS_KEY, modifiers.stream()
					.filter(Objects::nonNull)
					.map(NBTSerialization::serialize)
					.toArray(NBTCompound[]::new));
		}
	}

	/**
	 * Adds a series of modifiers to the rule.
	 *
	 * @param modifiers The modifiers to set.
	 *
	 * @apiNote If the rule has modifiers that you're setting, they'll be overwritten.
	 */
	public void addModifiers(ModifierData... modifiers) {
		if (Iteration.isNullOrEmpty(modifiers)) {
			return;
		}
		handleModifiers((current) -> {
			for (ModifierData modifier : modifiers) {
				if (modifier == null || !modifier.isValid()) {
					continue;
				}
				current.add(modifier);
			}
			return true;
		});
	}

	/**
	 * Attempts to find a modifier by its identifier.
	 *
	 * @param <T> The type of the modifier to cast the result to.
	 * @param slug The identifier of the modifier to base the search on.
	 * @return Returns the casted instance of the modifier, or null if nothing was found or if casting failed.
	 */
	@SuppressWarnings("unchecked")
	public <T extends ModifierData> T findModifier(String slug) {
		if (Strings.isNullOrEmpty(slug)) {
			return null;
		}
		for (ModifierData modifier : getModifiers()) {
			if (TextUtil.stringEqualsIgnoreCase(modifier.getSlug(), slug)) {
				try {
					return (T) modifier;
				}
				catch (Exception ignored) {
					break;
				}
			}
		}
		return null;
	}

	/**
	 * Removes a modifier by its identifier.
	 *
	 * @param slug The modifier identifier.
	 */
	public void removeModifiers(String slug) {
		if (Strings.isNullOrEmpty(slug)) {
			return;
		}
		handleModifiers((modifiers) -> {
			modifiers.removeIf((modifier) -> TextUtil.stringEqualsIgnoreCase(modifier.getSlug(), slug));
			return true;
		});
	}

	// ------------------------------------------------------------
	//
	// ------------------------------------------------------------

	/**
	 * Determines how many times this rule matches with the given inventory.
	 *
	 * @param inventory The inventory to determine stock within.
	 * @return Returns the amount of stock in the given inventory.
	 */
	public int calculateStock(Inventory inventory) {
		if (!InventoryAPI.isValidInventory(inventory)) {
			return 0;
		}
		if ()

		int amount = 0;
		for (ItemStack item : inventory.getContents()) {
			if (!ItemAPI.isValidItem(item) || !conforms(item)) {
				continue;
			}
			amount += item.getAmount();
		}
		if (amount <= 0) {
			return 0;
		}
		return Math.max(amount / getAmount(), 0);
	}

	/**
	 * Determines the stock items themselves from a given inventory, which can then be used for trade purposes.
	 *
	 * @param inventory The inventory to determine the stock within.
	 * @return An array of cloned items that can then be used within trade APIs.
	 */
	public ItemStack[] getStock(Inventory inventory) {
		ArrayList<ItemStack> stock = new ArrayList<>();
		if (!InventoryAPI.isValidInventory(inventory)) {
			return new ItemStack[0];
		}
		int requiredAmount = getAmount();
		for (ItemStack item : inventory.getContents()) {
			if (requiredAmount <= 0) {
				break;
			}
			if (!ItemAPI.isValidItem(item)) {
				continue;
			}
			if (!conforms(item)) {
				continue;
			}
			if (item.getAmount() <= requiredAmount) {
				stock.add(item.clone());
				requiredAmount -= item.getAmount();
			}
			else {
				ItemStack clone = item.clone();
				clone.setAmount(requiredAmount);
				stock.add(clone);
				requiredAmount = 0;
			}
		}
		if (requiredAmount > 0) {
			return new ItemStack[0];
		}
		return stock.toArray(new ItemStack[0]);
	}

	/**
	 * Converts this rule into an item.
	 *
	 * @return Returns an itemised representation of this rule.
	 */
	public ItemStack toItem() {
		ItemStack item = NBTCompound.processItem(ItemExchangePlugin.RULE_ITEM.clone(),
				(nbt) -> nbt.setCompound(RULE_KEY, NBTSerialization.serialize(this)));
		ItemAPI.handleItemMeta(item, (ItemMeta meta) -> {
			meta.setDisplayName(getRuleTitle());
			meta.setLore(getRuleDetails());
			return true;
		});
		return item;
	}

	/**
	 * Attempts to retrieve a third generation Exchange rule from an item.
	 *
	 * @param item The item to retrieve the Exchange rule from.
	 * @return Returns an exchange rule if found, or null.
	 */
	public static ExchangeRule fromItem(ItemStack item) {
		if (item == null) {
			return null;
		}
		if (item.getType() != ItemExchangePlugin.RULE_ITEM.getType()) {
			return null;
		}
		NBTCompound nbt = NBTCompound.fromItem(item).getCompound(RULE_KEY);
		if (!nbt.isEmpty()) {
			NBTSerializable serializable = NBTSerialization.deserialize(nbt);
			if (serializable instanceof ExchangeRule) {
				return (ExchangeRule) serializable;
			}
		}
		return null;
	}

}
