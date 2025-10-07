package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public final class FairPlay extends BasicHack {
    public FairPlay(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler
    public void on(PlayerJoinEvent event) {
        event.getPlayer().sendMessage(Component.empty()
            .append(Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.LIGHT_PURPLE),
                Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.DARK_AQUA)
                , Component.text(" ").color(NamedTextColor.GOLD)
                , Component.text(" ").color(NamedTextColor.YELLOW),
                Component.text("§f§a§i§r§x§a§e§r§o§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r")));
    }
}
