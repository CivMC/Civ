package com.untamedears.itemexchange.rules;

import static vg.civcraft.mc.civmodcore.util.Iteration.collect;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.castOrNull;
import static vg.civcraft.mc.civmodcore.util.NullCoalescing.equalsNotNull;

import com.google.common.base.Strings;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.ItemExchangePlugin;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import com.untamedears.itemexchange.rules.modifiers.DisplayNameModifier;
import com.untamedears.itemexchange.rules.modifiers.LoreModifier;
import com.untamedears.itemexchange.utility.ModifierStorage;
import com.untamedears.itemexchange.utility.Utilities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.api.InventoryAPI;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.api.ItemNames;
import vg.civcraft.mc.civmodcore.api.MaterialAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializationException;
import vg.civcraft.mc.civmodcore.util.EnumUtils;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.Validation;

/**
 * This class represents an exchange rule.
 */
public final class ExchangeRule implements ExchangeData {

	/**
	 * The type of rule.
	 */
	public enum Type {
		INPUT, OUTPUT
	}

	public static final short NEW = 0;

	public static final short ANY = -1;

	public static final short USED = -2;

	public static final short ERROR = -99;

	// ------------------------------------------------------------
	// NBT Keys
	// ------------------------------------------------------------

	private static final String RULE_KEY = "ExchangeRule";

	private static final String VERSION_KEY = "version";

	private static final String TYPE_KEY = "type";

	private static final String MATERIAL_KEY = "material";

	private static final String AMOUNT_KEY = "amount";

	private static final String MODIFIERS_KEY = "modifiers";

	private static final String LEGACY_DISPLAY_NAME_KEY = "displayName";

	private static final String LEGACY_IGNORE_DISPLAY_NAME_KEY = "ignoringDisplayName";

	private static final String LEGACY_LORE_KEY = "lore";

	// ------------------------------------------------------------
	// Instance fields
	// ------------------------------------------------------------

	private static final ItemExchangePlugin PLUGIN = ItemExchangePlugin.getInstance();

	private Type type;
	private Material material;
	private int amount;
	private final ModifierStorage modifiers = new ModifierStorage();

	/**
	 * Creates a third generation Exchange rule.
	 */
	public ExchangeRule() { }

	/**
	 * Base the exchange rule on the given item.
	 *
	 * @param type lol
	 * @param item The item to base this exchange data on.
	 */
	public ExchangeRule(Type type, ItemStack item) {
		this.type = type;
		this.material = item.getType();
		this.amount = item.getAmount();
		ItemExchangePlugin.modifierRegistrar().getModifiers()
				.map(template -> template.construct(item))
				.filter(Objects::nonNull)
				.forEachOrdered(this.modifiers::put);
	}

	@Override
	public boolean isBroken() {
		if (this.type == null) {
			return true;
		}
		if (!MaterialAPI.isValidItemMaterial(this.material)) {
			return true;
		}
		if (this.amount < 1) {
			return true;
		}
		if (this.modifiers.stream().anyMatch(ExchangeData::isBroken)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if an arbitrary item conforms to this exchange data's requirements.
	 *
	 * @param item The arbitrary item to check.
	 * @return Returns true if the given item conforms.
	 */
	public boolean conforms(ItemStack item) {
		PLUGIN.debug("Testing: " + item);
		PLUGIN.debug("Against: " + this);
		if (!equalsNotNull(this.material, item.getType())) {
			PLUGIN.debug("Material does not match.");
			return false;
		}
		if (item.getAmount() <= 0) {
			PLUGIN.debug("Item doesn't have an amount.");
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			PLUGIN.debug("No ItemMeta.");
			return false;
		}
		for (ModifierData modifier : this.modifiers) {
			if (!modifier.conforms(item)) {
				PLUGIN.debug("[" + modifier.getClass().getSimpleName() + "] Modifier rejected that.");
				return false;
			}
		}
		// DO NOT MATCH GROUPS HERE, THIS IS CHECKED IN ItemExchangeListener!
		return true;
	}

	/**
	 * Gets the type of this rule.
	 *
	 * @return Returns the type of this rule, which will never be null. Anything that doesn't fit into the
	 *     INPUT/OUTPUT dichotomy will be represented as BROKEN.
	 */
	public Type getType() {
		return this.type;
	}

	/**
	 * Sets the type of this rule.
	 *
	 * @param type The type to set.
	 */
	public void setType(Type type) {
		this.type = type;
	}

	/**
	 * Gets the material of this rule.
	 *
	 * @return Returns this rule's listed material, which may be null.
	 */
	public Material getMaterial() {
		if (!MaterialAPI.isValidItemMaterial(this.material)) {
			return null;
		}
		return this.material;
	}

	/**
	 * Sets the material of this rule.
	 *
	 * @param material The material to set to this rule.
	 */
	public void setMaterial(Material material) {
		this.material = material;
	}

	/**
	 * Gets the rule's amount.
	 *
	 * @return Return this rule's listed amount.
	 */
	public int getAmount() {
		return this.amount;
	}

	/**
	 * Sets the rule's amount.
	 *
	 * @param amount The amount to set to this rule.
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public ModifierStorage getModifiers() {
		return this.modifiers;
	}

	@Override
	public void serialize(NBTCompound nbt) throws NBTSerializationException {
		nbt.setInteger(VERSION_KEY, 4);
		nbt.setString(TYPE_KEY, EnumUtils.getSlug(this.type));
		nbt.setString(MATERIAL_KEY, EnumUtils.getSlug(this.material));
		nbt.setInteger(AMOUNT_KEY, this.amount);
		nbt.setCompoundArray(MODIFIERS_KEY, this.modifiers.stream()
				.map(NBTSerialization::serialize)
				.filter(Validation::checkValidity)
				.toArray(NBTCompound[]::new));
	}

	@Override
	public void deserialize(NBTCompound nbt) throws NBTSerializationException {
		this.type = EnumUtils.fromSlug(Type.class, nbt.getString(TYPE_KEY), false);
		this.material = EnumUtils.fromSlug(Material.class, nbt.getString(MATERIAL_KEY), false);
		this.amount = nbt.getInteger(AMOUNT_KEY);
		this.modifiers.clear();
		Arrays.stream(nbt.getCompoundArray(MODIFIERS_KEY))
				.map(raw -> castOrNull(ModifierData.class, NBTSerialization.deserialize(raw)))
				.forEachOrdered(this.modifiers::put);
		// Legacy Support
		if (nbt.hasKeyOfType(LEGACY_DISPLAY_NAME_KEY, 8) && !nbt.getBoolean(LEGACY_IGNORE_DISPLAY_NAME_KEY)) {
			DisplayNameModifier displayName = this.modifiers.get(DisplayNameModifier.class);
			if (displayName == null) {
				displayName = (DisplayNameModifier) DisplayNameModifier.TEMPLATE.construct();
				displayName.setDisplayName(nbt.getString(LEGACY_DISPLAY_NAME_KEY));
				this.modifiers.put(displayName);
			}
		}
		if (nbt.hasKeyOfType(LEGACY_LORE_KEY, 9)) {
			LoreModifier lore = this.modifiers.get(LoreModifier.class);
			if (lore == null) {
				lore = (LoreModifier) LoreModifier.TEMPLATE.construct();
				lore.setLore(collect(ArrayList::new, nbt.getStringArray(LEGACY_LORE_KEY)));
				this.modifiers.put(lore);
			}
		}
	}

	// ------------------------------------------------------------
	//
	// ------------------------------------------------------------

	/**
	 * Gets a listing for this rule. If none is set then one is automatically generated.
	 *
	 * @return Returns the rule's listing.
	 */
	private String getListing() {
		String listing = this.modifiers.stream()
				.sorted(Collections.reverseOrder())
				.map(ModifierData::getDisplayListing)
				.filter(Objects::nonNull)
				.findFirst().orElse(null);
		if (!Strings.isNullOrEmpty(listing)) {
			return listing;
		}
		listing = ItemNames.getItemName(this.material);
		if (!Strings.isNullOrEmpty(listing)) {
			return listing;
		}
		listing = EnumUtils.getSlug(this.material);
		if (!Strings.isNullOrEmpty(listing)) {
			return listing;
		}
		return "UNKNOWN";
	}

	private String getDisplayName() {
		DisplayNameModifier displayNameModifier = this.modifiers.get(DisplayNameModifier.class);
		if (displayNameModifier == null) {
			return null;
		}
		return displayNameModifier.getDisplayName();
	}

	/**
	 * Gets the details title of this exchange rule.
	 *
	 * @return Return this exchange rule's details title.
	 *
	 * @apiNote This is essentially the first line of {@link ExchangeRule#getRuleDetails()} ()} but needs to be
	 *     separate so that when creating a rule item, the item's display name is set to the title, and the
	 *     remainder of the details is set to the item's lore.
	 */
	private String getRuleTitle() {
		StringBuilder builder = new StringBuilder();
		builder.append(ChatColor.YELLOW);
		if (isBroken()) {
			builder.append("Broken");
		}
		else if (this.type == Type.INPUT) {
			builder.append("Input");
		}
		else if (this.type == Type.OUTPUT) {
			builder.append("Output");
		}
		builder
				.append(": ")
				.append(ChatColor.WHITE)
				.append(this.amount)
				.append(" ")
				.append(getListing());
		String displayName = getDisplayName();
		if (!Strings.isNullOrEmpty(displayName)) {
			builder
					.append(ChatColor.WHITE)
					.append(ChatColor.ITALIC)
					.append(" \"")
					.append(displayName)
					.append(ChatColor.WHITE)
					.append(ChatColor.ITALIC)
					.append("\"");
		}
		return builder.toString();
	}

	private List<String> getRuleDetails() {
		List<String> info = new ArrayList<>();
		this.modifiers.stream()
				.map(ModifierData::getDisplayInfo)
				.filter(list -> !Iteration.isNullOrEmpty(list))
				.forEachOrdered(info::addAll);
		return info;
	}

	@Override
	public List<String> getDisplayInfo() {
		List<String> info = new ArrayList<>();
		info.add(getRuleTitle());
		info.addAll(getRuleDetails());
		return info;
	}

	@Override
	public String toString() {
		return "ExchangeRule{type=" + this.type + ",material=" + this.material + ",amount=" + this.amount + "," +
				"modifiers={" + this.modifiers + "}}";
	}

	// ------------------------------------------------------------
	// Utilities
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
		int stock = 0;
		for (ItemStack item : inventory.getContents()) {
			if (!ItemAPI.isValidItem(item) || Utilities.isExchangeRule(item) || !conforms(item)) {
				continue;
			}
			stock += item.getAmount();
		}
		if (stock <= 0) {
			return 0;
		}
		return Math.max(stock / getAmount(), 0);
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
			if (!ItemAPI.isValidItem(item) || Utilities.isExchangeRule(item)) {
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
		ItemStack item = NBTCompound.processItem(ItemExchangeConfig.getRuleItem(),
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
		if (!ItemAPI.isValidItem(item)) {
			return null;
		}
		if (item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
			return null;
		}
		NBTSerializable serializable = NBTSerialization.deserialize(NBTCompound.fromItem(item).getCompound(RULE_KEY));
		if (serializable instanceof ExchangeRule) {
			return (ExchangeRule) serializable;
		}
		return null;
	}

}
