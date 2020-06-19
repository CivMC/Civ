package com.untamedears.itemexchange.rules;

import static vg.civcraft.mc.civmodcore.util.NullCoalescing.chain;

import com.google.common.collect.Lists;
import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.api.ItemAPI;
import vg.civcraft.mc.civmodcore.serialization.NBTCompound;
import vg.civcraft.mc.civmodcore.serialization.NBTSerializable;
import vg.civcraft.mc.civmodcore.serialization.NBTSerialization;
import vg.civcraft.mc.civmodcore.util.Iteration;
import vg.civcraft.mc.civmodcore.util.Validation;

public final class BulkExchangeRule implements ExchangeData {

	public static final String BULK_KEY = "BulkExchangeRule";

	public static final String RULES_KEY = "rules";

	private List<ExchangeRule> rules;

	@Override
	public boolean isBroken() {
		return Iteration.isNullOrEmpty(this.rules);
	}

	@Override
	public void serialize(NBTCompound nbt) {
		nbt.setCompoundArray(RULES_KEY, getRules().stream()
				.map(NBTSerialization::serialize)
				.filter(Objects::nonNull)
				.toArray(NBTCompound[]::new));
	}

	@Override
	public void deserialize(NBTCompound nbt) {
		setRules(Arrays.stream(nbt.getCompoundArray(RULES_KEY))
				.map(raw -> chain(() -> (ExchangeRule) NBTSerialization.deserialize(raw)))
				.collect(Collectors.toCollection(ArrayList::new)));
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
		ItemStack item = NBTCompound.processItem(ItemExchangeConfig.getRuleItem(), (nbt) ->
				nbt.setCompound(BULK_KEY, NBTSerialization.serialize(this)));
		ItemAPI.setDisplayName(item, ChatColor.RED + "Bulk Rule Block");
		ItemAPI.setLore(item, String.format("This rule block holds %s exchange rule%s.",
				rules.size(), rules.size() == 1 ? "" : "s"));
		return item;
	}

	public static BulkExchangeRule fromItem(ItemStack item) {
		if (!ItemAPI.isValidItem(item)) {
			return null;
		}
		if (item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
			return null;
		}
		NBTSerializable serializable = NBTSerialization.deserialize(NBTCompound.fromItem(item).getCompound(BULK_KEY));
		if (serializable instanceof BulkExchangeRule) {
			return (BulkExchangeRule) serializable;
		}
		return null;
	}

}
