package com.untamedears.itemexchange.rules;

import com.untamedears.itemexchange.ItemExchangeConfig;
import com.untamedears.itemexchange.rules.interfaces.ExchangeData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

public record BulkExchangeRule(List<ExchangeRule> rules) implements ExchangeData {

    public static final String BULK_KEY = "BulkExchangeRule";
    public static final String RULES_KEY = "rules";

    public BulkExchangeRule(@NotNull final List<ExchangeRule> rules) {
        this.rules = Objects.requireNonNull(rules);
    }

    @Override
    public boolean isBroken() {
        return this.rules.isEmpty();
    }

    @Override
    public void toNBT(@NotNull final NbtCompound nbt) {
        nbt.setCompoundArray(RULES_KEY, this.rules.stream()
            .map((rule) -> {
                final var ruleNBT = new NbtCompound();
                rule.toNBT(ruleNBT);
                return ruleNBT;
            })
            .toArray(NbtCompound[]::new));
    }

    @NotNull
    public static BulkExchangeRule fromNBT(@NotNull final NbtCompound nbt) {
        return new BulkExchangeRule(Arrays.stream(nbt.getCompoundArray(RULES_KEY, true))
            .map(ExchangeRule::fromNBT)
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    public ItemStack toItem() {
        ItemStack item = ItemExchangeConfig.getRuleItem();
		final var itemNBT = new NbtCompound();
		toNBT(itemNBT);

        CustomData.update(
            DataComponents.CUSTOM_DATA,
            CraftItemStack.unwrap(item),
            (nbt) -> nbt.put(BULK_KEY, itemNBT.internal())
        );

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
        if (item == null || item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
            return null;
        }
        final CustomData customData = CraftItemStack.unwrap(item).get(DataComponents.CUSTOM_DATA);
        if (customData == null) {
            return null;
        }
        final var itemNBT = new NbtCompound(customData.copyTag());
        final NbtCompound bulkNBT = itemNBT.getCompound(BULK_KEY, false);
        if (bulkNBT == null) {
            return null;
        }
        final NbtCompound[] rulesNBT = bulkNBT.getCompoundArray(RULES_KEY, true);
        final var rules = new ArrayList<ExchangeRule>(rulesNBT.length);
        for (final var ruleNBT : rulesNBT) {
            rules.add(ExchangeRule.fromNBT(ruleNBT));
        }
        return new BulkExchangeRule(rules);
    }
}
