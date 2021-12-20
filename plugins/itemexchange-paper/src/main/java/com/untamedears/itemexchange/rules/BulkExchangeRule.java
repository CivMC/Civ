package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.nbt.NBTSerialization;
import vg.civcraft.mc.civmodcore.nbt.NBTType;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

public record BulkExchangeRule(List<ExchangeRule> rules) implements ExchangeData {

	public static final String BULK_KEY = "BulkExchangeRule";
	public static final String RULES_KEY = "rules";

	public BulkExchangeRule(@Nonnull final List<ExchangeRule> rules) {
		this.rules = Objects.requireNonNull(rules);
	}

	@Override
	public boolean isBroken() {
		return this.rules.isEmpty();
	}

	@Override
	public void toNBT(@Nonnull final NBTCompound nbt) {
		nbt.setCompoundArray(RULES_KEY, this.rules.stream()
				.map((rule) -> {
					final var ruleNBT = new NBTCompound();
					rule.toNBT(ruleNBT);
					return ruleNBT;
				})
				.toArray(NBTCompound[]::new));
	}

	@Nonnull
	public static BulkExchangeRule fromNBT(@Nonnull final NBTCompound nbt) {
		return new BulkExchangeRule(Arrays.stream(nbt.getCompoundArray(RULES_KEY))
				.map(ExchangeRule::fromNBT)
				.collect(Collectors.toCollection(ArrayList::new)));
	}

	public ItemStack toItem() {
		final ItemStack item = NBTSerialization.processItem(ItemExchangeConfig.getRuleItem(), (nbt) -> {
			final var ruleNBT = new NBTCompound();
			toNBT(ruleNBT);
			nbt.put(BULK_KEY, ruleNBT);
		});
		ItemUtils.handleItemMeta(item, (ItemMeta meta) -> {
			meta.displayName(Component.text()
					.color(NamedTextColor.RED)
					.content("Bulk Rule Block")
					.build());
			MetaUtils.setComponentLore(meta, Component.text(
					String.format(
							"This rule block holds %s exchange rule%s.",
							this.rules.size(), this.rules.size() == 1 ? "" : "s")));
			return true;
		});
		return item;
	}

	@Nullable
	public static BulkExchangeRule fromItem(final ItemStack item) {
		if (!ItemUtils.isValidItem(item)
				|| item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
			return null;
		}
		final var meta = item.getItemMeta();
		if (meta == null) {
			return null;
		}
		// From NBT
		final var itemNBT = NBTSerialization.fromItem(item);
		if (itemNBT.hasKeyOfType(BULK_KEY, NBTType.COMPOUND)) {
			final var rulesNBT = itemNBT.getCompound(BULK_KEY).getCompoundArray(RULES_KEY);
			final var rules = new ArrayList<ExchangeRule>(rulesNBT.length);
			for (final var ruleNBT : rulesNBT) {
				rules.add(ExchangeRule.fromNBT(ruleNBT));
			}
			return new BulkExchangeRule(rules);
		}
		return null;
	}

}
