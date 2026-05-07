package xyz.huskydog.banstickVelocity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.huskydog.banstickCore.cmc.utils.PluginPlayer;
import java.net.InetSocketAddress;
import java.util.UUID;

public record ProxyPlayerWrapper(@NotNull UUID uuid, @NotNull String username, @Nullable InetSocketAddress address) implements PluginPlayer {

    @Override
    public @NotNull String getDisplayName() {
        return username;
    }

    @Override
    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    @Override
    public @Nullable InetSocketAddress getAddress() {
        return address;
    }
}
