package net.civmc.kitpvp.dao;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public record Kit(
    int id,
    String name,
    boolean isPublic,
    Material icon,
    ItemStack[] items
) {
}
