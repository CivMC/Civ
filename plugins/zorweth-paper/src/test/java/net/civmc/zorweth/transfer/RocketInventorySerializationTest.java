package net.civmc.zorweth.transfer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class RocketInventorySerializationTest {

    @Test
    public void rejectsNullInventoryContents() {
        Assertions.assertThrows(NullPointerException.class,
            () -> RocketInventorySerialization.serializeInventory(null));
    }

    @Test
    public void rejectsNegativeExpectedInventoryLength() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> RocketInventorySerialization.deserializeInventory(new byte[] {1, 2, 3}, -1));
    }
}
