package vg.civcraft.mc.civmodcore.mods.packets;

import com.google.gson.JsonElement;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface CivModPacketHandler {
    public static final CivModPacketHandler NOOP = (sender, packetId, json) -> {};

    public void handleCivModPacket(
        @NotNull Player sender,
        @NotNull Key packetId,
        @NotNull JsonElement json
    );
}
