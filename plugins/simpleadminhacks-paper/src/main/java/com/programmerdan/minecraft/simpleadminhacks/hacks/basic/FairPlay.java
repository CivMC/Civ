package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public final class FairPlay extends BasicHack implements PluginMessageListener {

    private static final String JOURNEYMAP_PERMISSION_CHANNEL = "journeymap:perm_req";
    private static final byte JOURNEYMAP_PERMISSION_PACKET_ID = 42;
    private static final String JOURNEYMAP_FAIRPLAY_PERMISSIONS = """
        {
          "caveMapping": "NONE",
          "caveRenderRange": "0",
          "radarEnabled": "NONE",
          "worldPlayerRadar": "NONE",
          "playerRadarEnabled": "false",
          "playerRadarNamesEnabled": "false",
          "villagerRadarEnabled": "false",
          "animalRadarEnabled": "false",
          "mobRadarEnabled": "false"
        }
        """;

    public FairPlay(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        Bukkit.getMessenger().registerIncomingPluginChannel(plugin(), JOURNEYMAP_PERMISSION_CHANNEL, this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(plugin(), JOURNEYMAP_PERMISSION_CHANNEL);
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterIncomingPluginChannel(plugin(), JOURNEYMAP_PERMISSION_CHANNEL, this);
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(plugin(), JOURNEYMAP_PERMISSION_CHANNEL);
        super.onDisable();
    }

    @Override
    public void onPluginMessageReceived(@NotNull final String channel, @NotNull final Player player,
                                        final byte @NotNull [] message) {
        if (!JOURNEYMAP_PERMISSION_CHANNEL.equals(channel)
            || message.length == 0
            || message[0] != JOURNEYMAP_PERMISSION_PACKET_ID) {
            return;
        }
        sendJourneyMapPermissions(player);
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

    private void sendJourneyMapPermissions(final Player player) {
        final ByteArrayDataOutput output = ByteStreams.newDataOutput();
        final byte[] payload = JOURNEYMAP_FAIRPLAY_PERMISSIONS.getBytes();
        output.writeByte(JOURNEYMAP_PERMISSION_PACKET_ID);
        output.writeBoolean(false);
        writeVarInt(output, payload.length);
        output.write(payload);
        player.sendPluginMessage(plugin(), JOURNEYMAP_PERMISSION_CHANNEL, output.toByteArray());
    }

    private void writeVarInt(final ByteArrayDataOutput output, final int value) {
        int remaining = value;
        while ((remaining & ~0x7F) != 0) {
            output.writeByte((remaining & 0x7F) | 0x80);
            remaining >>>= 7;
        }
        output.writeByte(remaining);
    }
}
