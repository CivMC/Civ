package net.civmc.zorweth.transfer;

import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.GameMode;

public record RocketPassengerTransfer(
    UUID transferId,
    UUID playerUuid,
    RocketEntityPosition relativePosition,
    byte[] serializedInventory,
    double health,
    int xpLevel,
    float xpProgress,
    int foodLevel,
    float saturation,
    float exhaustion,
    int heldSlot,
    GameMode gameMode,
    RocketTransferPlayerState state
) {

    public RocketPassengerTransfer {
        Objects.requireNonNull(transferId, "transferId");
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(relativePosition, "relativePosition");
        Objects.requireNonNull(serializedInventory, "serializedInventory");
        Objects.requireNonNull(gameMode, "gameMode");
        Objects.requireNonNull(state, "state");
        serializedInventory = Arrays.copyOf(serializedInventory, serializedInventory.length);
    }

    @Override
    public byte[] serializedInventory() {
        return Arrays.copyOf(this.serializedInventory, this.serializedInventory.length);
    }
}
