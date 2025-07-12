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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;
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

    public ItemStack toItem() {
        ItemStack item = ItemExchangeConfig.getRuleItem();
		final var itemNBT = new NBTCompound();
		toNBT(itemNBT);

		CustomData customData = CustomData.EMPTY.update(nbt -> nbt.put(BULK_KEY, itemNBT.getRAW()));

		net.minecraft.world.item.ItemStack nmsItem = ItemUtils.getNMSItemStack(item);
		nmsItem.set(DataComponents.CUSTOM_DATA, customData);
		item = nmsItem.getBukkitStack();

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
        final CustomData itemNBT = ItemUtils.getNMSItemStack(item).get(DataComponents.CUSTOM_DATA);
        if (itemNBT != null && itemNBT.copyTag().contains(BULK_KEY)) {
            var t = new NBTCompound(itemNBT.copyTag());

            final var rulesNBT = t.getCompound(BULK_KEY).getCompoundArray(RULES_KEY);
            final var rules = new ArrayList<ExchangeRule>(rulesNBT.length);
            for (final var ruleNBT : rulesNBT) {
                rules.add(ExchangeRule.fromNBT(ruleNBT));
            }
            return new BulkExchangeRule(rules);
        }
        return null;
    }

}
