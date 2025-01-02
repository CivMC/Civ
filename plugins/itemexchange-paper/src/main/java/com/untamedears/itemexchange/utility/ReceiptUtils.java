package com.untamedears.itemexchange.utility;

import com.untamedears.itemexchange.rules.ExchangeRule;
import com.untamedears.itemexchange.rules.interfaces.DisplayContext;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import vg.civcraft.mc.civmodcore.chat.Componentify;
import vg.civcraft.mc.civmodcore.inventory.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.MetaUtils;

public final class ReceiptUtils {
    private static final ItemStack RECEIPT_ITEM; static {
        RECEIPT_ITEM = new ItemStack(Material.STONE_BUTTON);
        final ItemMeta meta = RECEIPT_ITEM.getItemMeta();
        meta.itemName(ChatUtils.nonItalic("Exchange Receipt", NamedTextColor.GOLD).build());
        meta.setCustomModelData(-44_054_986);
        RECEIPT_ITEM.setItemMeta(meta);
    }

    public static void init() {
        CustomItem.registerCustomItem("ie_receipt", RECEIPT_ITEM);
    }

    /**
     * @param shopBlock The block the purchaser interacted with when interacting with the shop.
     *
     * @param stockBlock The block the exchange originated from (where the input items will be deposited into, and
     *                   output items withdrawn from). Relays mean this may be different from 'shopBlock'.
     *
     * @param footer Should be provided by the {@link com.untamedears.itemexchange.rules.modifiers.ReceiptModifier} on
     *               the input rule.
     */
    public static @NotNull ItemStack generateReceipt(
        final @NotNull Player purchaser,
        final @NotNull Block shopBlock,
        final @NotNull Block stockBlock,
        final @NotNull ExchangeRule inputRule,
        final ExchangeRule outputRule,
        final String footer
    ) {
        final ItemStack receipt = RECEIPT_ITEM.clone();
        final ItemMeta meta = receipt.getItemMeta();
        final List<Component> lore = MetaUtils.getComponentLore(meta);
        lore.addAll(List.of(
            ChatUtils.nonItalic()
                .append(Component.text("Purchaser: ", NamedTextColor.YELLOW))
                .append(purchaser.name().color(NamedTextColor.WHITE))
                .build(),
            ChatUtils.nonItalic()
                .append(Component.text("Timestamp: ", NamedTextColor.YELLOW))
                .append(Component.text(formatTimestamp(System.currentTimeMillis()), NamedTextColor.LIGHT_PURPLE))
                .build(),
            ChatUtils.nonItalic()
                .append(Component.text("Shop block: ", NamedTextColor.YELLOW))
                .append(Componentify.blockLocation(shopBlock.getLocation()).color(NamedTextColor.WHITE))
                .build(),
            ChatUtils.nonItalic()
                .append(Component.text("Shop hash: ", NamedTextColor.YELLOW))
                .append(Component.text(formatShopHash(stockBlock), NamedTextColor.GRAY))
                .build(),
            Component.space()
        ));
        addRuleLore(lore, inputRule);
        if (outputRule != null) {
            lore.add(Component.space());
            addRuleLore(lore, outputRule);
        }
        if (StringUtils.isNotBlank(footer)) {
            lore.addAll(List.of(
                Component.space(),
                Component.text(footer, NamedTextColor.GOLD)
            ));
        }
        lore.addAll(List.of(
            Component.space(),
            Component.text(UUID.randomUUID().toString())
        ));
        meta.lore(lore);
        receipt.setItemMeta(meta);
        return receipt;
    }

    /**
     * Appends the typical chat-output to the item-lore, prefixed with a single space.
     */
    private static void addRuleLore(
        final @NotNull List<@NotNull Component> lore,
        final @NotNull ExchangeRule rule
    ) {
        for (final String line : rule.getDisplayInfo(DisplayContext.CHAT_OUTPUT)) {
            lore.add(LegacyComponentSerializer.legacySection().deserialize(" " + line).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        }
    }

    /**
     * @return Returns a formatted timestamp, eg: Tue, 3 Jun 2008 11:05:30 GMT
     */
    private static @NotNull String formatTimestamp(
        final long timestamp
    ) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC));
    }

    private static @NotNull String formatShopHash(
        final @NotNull Block stockBlock
    ) {
        return "#" + Integer.toHexString(stockBlock.getLocation().hashCode()).toUpperCase();
    }
}
