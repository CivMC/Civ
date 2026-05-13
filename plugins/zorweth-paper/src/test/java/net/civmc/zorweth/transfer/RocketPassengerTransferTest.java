package net.civmc.zorweth.transfer;

import java.util.UUID;
import org.bukkit.GameMode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RocketPassengerTransferTest {

    @Test
    public void copiesSerializedInventory() {
        final byte[] inventory = new byte[] {1, 2, 3};
        final RocketPassengerTransfer transfer = new RocketPassengerTransfer(
            UUID.randomUUID(),
            UUID.randomUUID(),
            new RocketEntityPosition(1.0, 2.0, 3.0, 90.0f, 45.0f),
            inventory,
            20.0,
            10,
            0.5f,
            20,
            5.0f,
            0.0f,
            2,
            GameMode.SURVIVAL,
            RocketTransferPlayerState.PENDING
        );

        inventory[0] = 9;
        final byte[] returned = transfer.serializedInventory();
        returned[1] = 9;

        Assertions.assertArrayEquals(new byte[] {1, 2, 3}, transfer.serializedInventory());
    }
}
