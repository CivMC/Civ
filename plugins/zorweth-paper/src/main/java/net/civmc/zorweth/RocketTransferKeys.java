package net.civmc.zorweth;

import org.bukkit.NamespacedKey;

public final class RocketTransferKeys {

    public static final NamespacedKey SOURCE_TRANSFER_ID = new NamespacedKey("zorweth", "source_transfer_id");
    public static final NamespacedKey SOURCE_CLEARED = new NamespacedKey("zorweth", "source_cleared");
    public static final NamespacedKey DESTINATION_TRANSFER_ID = new NamespacedKey("zorweth", "destination_transfer_id");
    public static final NamespacedKey DESTINATION_APPLIED_LOCAL = new NamespacedKey("zorweth", "destination_applied_local");
    public static final NamespacedKey SCHEMATIC_PASTED_LOCAL = new NamespacedKey("zorweth", "schematic_pasted_local");
    public static final NamespacedKey CHESTS_APPLIED_LOCAL = new NamespacedKey("zorweth", "chests_applied_local");

    private RocketTransferKeys() {
    }
}
