package com.untamedears.itemexchange.items;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

public record Token(
    @NotNull String token
) {
    private static final String TOKEN_KEY = "ie:token";
    private static final Material TOKEN_MATERIAL = Material.STONE_BUTTON;

    public Token {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        token = token.trim();
        final int length = token.length();
        if (length < 1) {
            throw new IllegalArgumentException("Token cannot be empty");
        }
        else if (length > 32) {
            throw new IllegalArgumentException("Token cannot be longer than 32 characters");
        }
        if (StringUtils.containsWhitespace(token)) {
            throw new IllegalArgumentException("Token cannot contain whitespace");
        }
    }

    public static @Nullable Token create(
        final String raw
    ) {
        try {
            return new Token(raw);
        }
        catch (final Exception ignored) {
            return null;
        }
    }

    @Override
    public boolean equals(
        final Object obj
    ) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof final Token other)) {
            return false;
        }
        return Strings.CI.equals(
            this.token(),
            other.token()
        );
    }

    public @NotNull ItemStack asItem() {
        final var item = new ItemStack(TOKEN_MATERIAL);
        item.setData(
            DataComponentTypes.ITEM_NAME,
            Component.textOfChildren(
                Component.text(
                    "Token: ",
                    Style.style()
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .build()
                ),
                Component.text(
                    this.token(),
                    Style.style()
                        .color(NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                        .build()
                )
            )
        );
        item.setData(
            DataComponentTypes.CUSTOM_MODEL_DATA,
            CustomModelData.customModelData()
                .addString(TOKEN_KEY)
                .build()
        );
        ItemUtils.editCustomData(item, (nbt) -> nbt.putString(TOKEN_KEY, this.token()));
        return item;
    }

    public static @Nullable Token fromItem(
        final ItemStack item
    ) {
        if (item == null || item.getType() != TOKEN_MATERIAL) {
            return null;
        }
        final CompoundTag nbt = ItemUtils.inspectCustomData(item);
        if (nbt == null) {
            return null;
        }
        if (!(nbt.get(TOKEN_KEY) instanceof StringTag(String token))) {
            return null;
        }
        return create(token);
    }
}
