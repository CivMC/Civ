package com.untamedears.itemexchange.rules;

import com.google.common.collect.Lists;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.apache.commons.collections4.CollectionUtils;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerialization;
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;
import vg.civcraft.mc.civmodcore.utilities.MoreClassUtils;

public final class BulkExchangeRule implements ExchangeData {

	public static final String BULK_KEY = "BulkExchangeRule";
	public static final String RULES_KEY = "rules";

	private List<ExchangeRule> rules;

	@Override
	public boolean isBroken() {
		return CollectionUtils.isEmpty(this.rules);
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.setCompoundArray(RULES_KEY, getRules().stream()
				.map((rule) -> {
					final var ruleNBT = new NBTCompound();
					rule.toNBT(ruleNBT);
					return ruleNBT;
				})
				.toArray(NBTCompound[]::new));
	}

	@Nonnull
	public static BulkExchangeRule fromNBT(@Nonnull final NBTCompound nbt) {
		final var rule = new BulkExchangeRule();
		rule.setRules(Arrays.stream(nbt.getCompoundArray(RULES_KEY))
				.map(raw -> MoreClassUtils.castOrNull(ExchangeRule.class, ExchangeRule.fromNBT(raw)))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(ArrayList::new)));
		return rule;
	}

	@Override
	public List<String> getDisplayInfo() {
		return new ArrayList<>();
	}

	// ------------------------------------------------------------
	// Getters + Setters
	// ------------------------------------------------------------

	public List<ExchangeRule> getRules() {
		if (this.rules == null) {
			return Lists.newArrayList();
		}
		return this.rules;
	}

	public void setRules(List<ExchangeRule> rules) {
		this.rules = rules;
	}

	public ItemStack toItem() {
		ItemStack item = NBTSerialization.processItem(ItemExchangeConfig.getRuleItem(), (nbt) -> {
			final var ruleNBT = new NBTCompound();
			toNBT(ruleNBT);
			nbt.set(BULK_KEY, ruleNBT);
		});
		ItemUtils.setDisplayName(item, ChatColor.RED + "Bulk Rule Block");
		ItemUtils.setLore(item, String.format("This rule block holds %s exchange rule%s.",
				rules.size(), rules.size() == 1 ? "" : "s"));
		return item;
	}

	public static BulkExchangeRule fromItem(ItemStack item) {
		if (!ItemUtils.isValidItem(item)) {
			return null;
		}
		if (item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
			return null;
		}
		final var itemNBT = NBTSerialization.fromItem(item);
		if (itemNBT.hasKeyOfType(BULK_KEY, NBTType.COMPOUND)) {
			return null;
		}
		return fromNBT(itemNBT.getCompound(BULK_KEY));
	}

}
