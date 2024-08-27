package vg.civcraft.mc.civmodcore.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public final class Componentify {
    private static @NotNull Component INTERNAL_addLocationWorld(
        final Location location
    ) {
        if (location == null || !location.isWorldLoaded()) {
            return Component.text()
                .color(NamedTextColor.RED)
                .content("<null>")
                .hoverEvent(HoverEvent.showText(Component.text("World not specified / loaded")))
                .build();
        }
        return Component.text()
            .content(location.getWorld().getName())
            .hoverEvent(HoverEvent.showText(Component.text("World name")))
            .build();
    }

    public static @NotNull ComponentLike @NotNull [] fullLocation(
        final @NotNull Location location
    ) {
        return new ComponentLike[] {
            INTERNAL_addLocationWorld(location),
            Component.space(),
            Component.text()
                .color(NamedTextColor.RED)
                .content(String.valueOf(location.getX()))
                .hoverEvent(HoverEvent.showText(Component.text("X"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.GREEN)
                .content(String.valueOf(location.getY()))
                .hoverEvent(HoverEvent.showText(Component.text("Y"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.BLUE)
                .content(String.valueOf(location.getZ()))
                .hoverEvent(HoverEvent.showText(Component.text("Z"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.GOLD)
                .content(String.valueOf(location.getYaw()))
                .hoverEvent(HoverEvent.showText(Component.text("Yaw"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.AQUA)
                .content(String.valueOf(location.getPitch()))
                .hoverEvent(HoverEvent.showText(Component.text("Pitch")))
        };
    }

    public static @NotNull ComponentLike @NotNull [] blockLocation(
        final @NotNull Location location
    ) {
        return new ComponentLike[] {
            INTERNAL_addLocationWorld(location),
            Component.space(),
            Component.text()
                .color(NamedTextColor.RED)
                .content(String.valueOf(location.getBlockX()))
                .hoverEvent(HoverEvent.showText(Component.text("Block X"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.GREEN)
                .content(String.valueOf(location.getBlockY()))
                .hoverEvent(HoverEvent.showText(Component.text("Block Y"))),
            Component.space(),
            Component.text()
                .color(NamedTextColor.BLUE)
                .content(String.valueOf(location.getBlockZ()))
                .hoverEvent(HoverEvent.showText(Component.text("Block Z")))
        };
    }
}
