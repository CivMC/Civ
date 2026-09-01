package vg.civcraft.mc.civmodcore.mods.packets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CivModPackets {
    private static final Logger LOGGER = LoggerFactory.getLogger(CivModPackets.class);
    private static final Gson GSON = new Gson();

    public static void registerPacket(
        final @NotNull JavaPlugin plugin,
        final @NotNull Key packetId,
        final boolean registerIncoming,
        final boolean registerOutgoing,
        final @NotNull CivModPacketHandler handler
    ) {
        Objects.requireNonNull(plugin);
        Objects.requireNonNull(packetId);
        Objects.requireNonNull(handler);
        final Messenger messenger = Bukkit.getMessenger();
        if (registerIncoming) {
            messenger.registerIncomingPluginChannel(plugin, packetId.toString(), (channel, sender, payload) -> {
                final Key receivedPacketId;
                try {
                    receivedPacketId = Key.key(channel);
                }
                catch (final Exception e) {
                    LOGGER.warn(
                        "{} sent a packet with an invalid packetId: {}",
                        sender.getName(),
                        channel
                    );
                    return;
                }
                final JsonElement json; {
                    final String raw = new String(payload, StandardCharsets.UTF_8);
                    try {
                        synchronized (GSON) {
                            json = GSON.fromJson(raw, JsonElement.class);
                        }
                    }
                    catch (final Exception e) {
                        LOGGER.warn(
                            "{} sent a packet [packetId:{}] with an invalid payload: {}",
                            sender.getName(),
                            receivedPacketId,
                            raw
                        );
                        return;
                    }
                }
                LOGGER.info(
                    "{} sent packet [packetId:{}]: {}",
                    sender.getName(),
                    receivedPacketId,
                    json
                );
                try {
                    handler.handleCivModPacket(sender, receivedPacketId, json);
                }
                catch (final Exception e) {
                    LOGGER.warn(
                        "Something went wrong while handling {}'s [packetId:{}] with payload: {}",
                        sender.getName(),
                        receivedPacketId,
                        json
                    );
                }
            });
        }
        if (registerOutgoing) {
            messenger.registerOutgoingPluginChannel(plugin, packetId.toString());
        }
    }

    /// Use this to register incoming-only packets. If the packet is bidirectional, use [#registerPacket] instead.
    public static void registerIncomingPacket(
        final @NotNull JavaPlugin plugin,
        final @NotNull Key packetId,
        final @NotNull CivModPacketHandler handler
    ) {
        registerPacket(plugin, packetId, true, false, handler);
    }

    /// Use this to register outgoing-only packets. If the packet is bidirectional, use [#registerPacket] instead.
    public static void registerOutgoingPacket(
        final @NotNull JavaPlugin plugin,
        final @NotNull Key packetId
    ) {
        registerPacket(plugin, packetId, false, true, CivModPacketHandler.NOOP);
    }

    public static void sendPacket(
        final @NotNull JavaPlugin plugin,
        final @NotNull Player recipient,
        final @NotNull Key packetId,
        final @NotNull JsonElement json
    ) {
        final String payload;
        synchronized (GSON) {
            payload = GSON.toJson(json);
        }
        LOGGER.info(
            "Sending packet to {}: [packetId:{}]: {}",
            recipient.getName(),
            packetId,
            payload
        );
        recipient.sendPluginMessage(
            plugin,
            packetId.toString(),
            payload.getBytes(StandardCharsets.UTF_8)
        );
    }
}


