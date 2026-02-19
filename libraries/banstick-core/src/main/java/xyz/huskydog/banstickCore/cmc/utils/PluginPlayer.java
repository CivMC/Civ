package xyz.huskydog.banstickCore.cmc.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.net.InetSocketAddress;
import java.util.UUID;

public interface PluginPlayer {
    /// Get display name of the player, likely their civ name
    @NotNull String getDisplayName();

    /// Get player UUID
    @NotNull UUID getUniqueId();

    @Nullable InetSocketAddress getAddress();
}
