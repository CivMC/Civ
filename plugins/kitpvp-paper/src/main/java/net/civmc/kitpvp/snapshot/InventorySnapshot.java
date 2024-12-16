package net.civmc.kitpvp.snapshot;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.inventory.ItemStack;

public record InventorySnapshot(
    ItemStack[] items,
    boolean victim,
    PlayerProfile otherPlayerProfile,
    double health
) {

}
