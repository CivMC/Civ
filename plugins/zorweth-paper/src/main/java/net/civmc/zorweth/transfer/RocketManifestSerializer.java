package net.civmc.zorweth.transfer;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public final class RocketManifestSerializer {

    private RocketManifestSerializer() {
    }

    public static List<RocketPassengerTransfer> serializePassengers(final RocketManifest manifest) {
        final List<RocketPassengerTransfer> passengers = new ArrayList<>();
        for (final RocketManifestPassenger passenger : manifest.passengers()) {
            passengers.add(new RocketPassengerTransfer(
                manifest.transferId(),
                passenger.playerUuid(),
                passenger.relativePosition(),
                ItemStack.serializeItemsAsBytes(passenger.inventoryContents()),
                passenger.health(),
                passenger.xpLevel(),
                passenger.xpProgress(),
                passenger.foodLevel(),
                passenger.saturation(),
                passenger.exhaustion(),
                passenger.heldSlot(),
                passenger.gameMode(),
                RocketTransferPlayerState.PENDING
            ));
        }
        return passengers;
    }

    public static List<RocketChestTransfer> serializeChests(final RocketManifest manifest) {
        final List<RocketChestTransfer> chests = new ArrayList<>();
        for (final RocketManifestChest chest : manifest.chests()) {
            chests.add(new RocketChestTransfer(
                manifest.transferId(),
                chest.relativePosition(),
                ItemStack.serializeItemsAsBytes(chest.contents()),
                RocketTransferCargoState.PENDING
            ));
        }
        return chests;
    }
}
