package com.untamedears.itemexchange.rules.modifiers;

import com.untamedears.itemexchange.items.Token;
import com.untamedears.itemexchange.rules.interfaces.Modifier;
import com.untamedears.itemexchange.rules.interfaces.ModifierData;
import java.util.List;
import net.minecraft.nbt.StringTag;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import vg.civcraft.mc.civmodcore.nbt.NbtCompound;

@Modifier(slug = "TOKEN", order = 50)
public final class TokenModifier extends ModifierData {
    public static final TokenModifier TEMPLATE = new TokenModifier();

    public static final String TOKEN_KEY = "token";

    public volatile Token token = null;

    @Override
    public @Nullable TokenModifier construct(
        final ItemStack item
    ) {
        final Token itemToken = Token.fromItem(item);
        if (itemToken == null) {
            return null;
        }
        final var modifier = new TokenModifier();
        modifier.token = itemToken;
        return modifier;
    }

    @Override
    public boolean isBroken() {
        return false;
    }

    @Override
    public boolean conforms(
        final ItemStack item
    ) {
        final Token itemToken = Token.fromItem(item);
        if (itemToken == null) {
            return false;
        }
        if (this.token instanceof final Token selfToken) {
            return selfToken.equals(itemToken);
        }
        return true;
    }

    @Override
    public void toNBT(
        final @NotNull NbtCompound nbt
    ) {
        switch (this.token) {
            case Token(String tokenString) -> nbt.setString(TOKEN_KEY, tokenString);
            case null -> nbt.remove(TOKEN_KEY);
        }
    }

    public static @NotNull TokenModifier fromNBT(
        final @NotNull NbtCompound nbt
    ) {
        final var modifier = new TokenModifier();
        modifier.token = switch (nbt.internal().get(TOKEN_KEY)) {
            case StringTag(String tokenString) -> Token.create(tokenString);
            case null, default -> null;
        };
        return modifier;
    }

    @Override
    public @NotNull String getDisplayListing() {
        return ChatColor.GRAY + "Token";
    }

    @Override
    public @Nullable List<@NotNull String> getDisplayInfo() {
        return switch (this.token) {
            case Token $ -> null;
            case null -> List.of(ChatColor.GOLD + "Any token");
        };
    }

    @Override
    public String toString() {
        return getSlug()
            + "{"
            + "token=" + this.token
            + "}";
    }
}
