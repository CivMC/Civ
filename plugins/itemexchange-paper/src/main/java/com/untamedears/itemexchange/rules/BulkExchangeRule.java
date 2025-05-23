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
import net.minecraft.nbt.CompoundTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.nbt.wrappers.NBTCompound;

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
    public void toNBT(@NotNull final NBTCompound nbt) {
        nbt.setCompoundArray(RULES_KEY, this.rules.stream()
            .map((rule) -> {
                final var ruleNBT = new NBTCompound();
                rule.toNBT(ruleNBT);
                return ruleNBT;
            })
            .toArray(NBTCompound[]::new));
    }

    @NotNull
    public static BulkExchangeRule fromNBT(@NotNull final NBTCompound nbt) {
        return new BulkExchangeRule(Arrays.stream(nbt.getCompoundArray(RULES_KEY))
            .map(ExchangeRule::fromNBT)
            .collect(Collectors.toCollection(ArrayList::new)));
    }

    public @NotNull ItemStack toItem() {
        final ItemStack item = ItemExchangeConfig.getRuleItem();
        ItemUtils.editCustomData(item, (nbt) -> {
            final var bulkNBT = new CompoundTag();
            toNBT(new NBTCompound(bulkNBT));
            nbt.put(BULK_KEY, bulkNBT);
        });
        ItemUtils.setDisplayName(item,
            Component.text()
                .color(NamedTextColor.RED)
                .content("Bulk Rule Block")
                .build()
        );
        ItemUtils.setLore(item, List.of(
            Component.text("This rule block holds %s exchange rule%s.".formatted(
                this.rules.size(),
                this.rules.size() == 1 ? "" : "s"
            ))
        ));
        return item;
    }

    public static @Nullable BulkExchangeRule fromItem(
        final ItemStack item
    ) {
        if (item == null || item.getType() != ItemExchangeConfig.getRuleItemMaterial()) {
            return null;
        }
        final CompoundTag root = ItemUtils.getCustomData(item);
        if (root == null) {
            return null;
        }
        final NBTCompound bulkNBT;
        if (root.get(BULK_KEY) instanceof final CompoundTag nbt) {
            bulkNBT = new NBTCompound(nbt);
        }
        else {
            return null;
        }
        final ExchangeRule[] rules; {
            final NBTCompound[] rulesNBT = bulkNBT.getCompoundArray(RULES_KEY);
            rules = new ExchangeRule[rulesNBT.length];
            for (int i = 0; i < rulesNBT.length; i++) {
                rules[i] = ExchangeRule.fromNBT(rulesNBT[i]);
            }
        }
        return new BulkExchangeRule(Arrays.asList(rules));
    }

}
