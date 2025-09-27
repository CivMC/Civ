package xyz.huskydog.queue;

import net.kyori.adventure.text.Component;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MinestomPlayer extends Player {
    // private static final VarHandle LATENCY;
    // private static final VarHandle DISPLAY_NAME;
    private static boolean isIn_UNSAFE_init;
    private static boolean isIn_remove;
    private static boolean isIn_setGameMode;

    public MinestomPlayer(@NotNull PlayerConnection playerConnection, @NotNull GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    @Override
    public void sendPacket(@NotNull SendablePacket packet) {
        if (isIn_UNSAFE_init || isIn_remove || isIn_setGameMode) {
            switch (packet) {
                // prevent player from receiving tab list
                case PlayerInfoUpdatePacket p -> {}
                case PlayerInfoRemovePacket p -> {}
                case CachedPacket c -> {
                    var p = c.packet(playerConnection.getConnectionState());
                    if (p instanceof PlayerInfoUpdatePacket) {
                    } else if (p instanceof PlayerInfoRemovePacket) {
                    } else {
                        super.sendPacket(c);
                    }
                }
                default -> super.sendPacket(packet);
            }
        } else {
            super.sendPacket(packet);
        }
    }

    @Override
    public CompletableFuture<Void> UNSAFE_init() {
        isIn_UNSAFE_init = true;
        var fut = super.UNSAFE_init();
        isIn_UNSAFE_init = false;
        return fut;
    }

    @Override
    public void remove(boolean permanent) {
        isIn_remove = true;
        super.remove(permanent);
        isIn_remove = false;
    }

    // @Override
    // public void refreshLatency(int latency) {
    //     LATENCY.setVolatile(this, latency);
    // }

    // @Override
    // public void setDisplayName(@Nullable Component displayName) {
    //     DISPLAY_NAME.set(this, displayName);
    // }

    @Override
    public boolean setGameMode(@NotNull GameMode gameMode) {
        isIn_setGameMode = true;
        var r = super.setGameMode(gameMode);
        isIn_setGameMode = false;
        return r;
    }

    public void sendPacketDirect(@NotNull SendablePacket packet) {
        super.sendPacket(packet);
    }

    @Override
    public @NotNull PlayerInfoUpdatePacket getAddPlayerToList() {
        return super.getAddPlayerToList();
    }

    @Override
    public @NotNull PlayerInfoRemovePacket getRemovePlayerToList() {
        return super.getRemovePlayerToList();
    }

    // static {
    //     try {
    //         var lookup = MethodHandles.privateLookupIn(Player.class, MethodHandles.lookup());
    //         LATENCY = lookup.findVarHandle(Player.class, "latency", int.class);
    //         DISPLAY_NAME = lookup.findVarHandle(Player.class, "displayName", Component.class);
    //     } catch (Throwable t) {
    //         throw new Error(t);
    //     }
    // }
}
