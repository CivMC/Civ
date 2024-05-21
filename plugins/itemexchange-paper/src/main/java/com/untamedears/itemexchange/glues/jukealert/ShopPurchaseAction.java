package com.untamedears.itemexchange.glues.jukealert;

import com.untamedears.jukealert.model.Snitch;
import com.untamedears.jukealert.model.actions.LoggedActionPersistence;
import com.untamedears.jukealert.model.actions.abstr.LoggableAction;
import com.untamedears.jukealert.model.actions.abstr.LoggablePlayerAction;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.gui.DecorationStack;
import vg.civcraft.mc.civmodcore.inventory.gui.IClickable;
import vg.civcraft.mc.civmodcore.inventory.items.ItemUtils;

final class ShopPurchaseAction extends LoggablePlayerAction {
    public static final String IDENTIFIER = "SHOP_PURCHASE_ACTION";

    private final Location location;

    public ShopPurchaseAction(
        final @NotNull Snitch snitch,
        final @NotNull UUID purchaser,
        final @NotNull Location location,
        final long timestamp
    ) {
        super(timestamp, snitch, purchaser);
        this.location = location;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public boolean hasPlayer() {
        return true;
    }

    @Override
    public @NotNull String getChatRepresentationIdentifier() {
        return "ItemExchange purchase";
    }

    @Override
    public @NotNull IClickable getGUIRepresentation() {
        final var icon = new ItemStack(Material.CHEST);
        super.enrichGUIItem(icon);
        ItemUtils.addComponentLore(
            icon,
            Component.text()
                .decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE)
                .color(NamedTextColor.GOLD)
                .content("Location: ")
                .append(
                    Component.text(this.location.getWorld().getName(), NamedTextColor.WHITE),
                    Component.space(),
                    Component.text(this.location.getBlockX(), NamedTextColor.RED),
                    Component.space(),
                    Component.text(this.location.getBlockY(), NamedTextColor.GREEN),
                    Component.space(),
                    Component.text(this.location.getBlockZ(), NamedTextColor.BLUE)
                )
                .build()
    );
        return new DecorationStack(icon);
    }

    @Override
    public @NotNull LoggedActionPersistence getPersistence() {
        return new LoggedActionPersistence(
            this.time,
            this.player,
            this.location.getBlockX(),
            this.location.getBlockY(),
            this.location.getBlockZ(),
            null
        );
    }

    public static @NotNull LoggableAction provider(
        final @NotNull Snitch snitch,
        final @NotNull UUID playerUuid,
        final @NotNull Location location,
        final long timestamp,
        final @NotNull String extra
    ) {
        return new ShopPurchaseAction(snitch, playerUuid, location, timestamp);
    }
}
