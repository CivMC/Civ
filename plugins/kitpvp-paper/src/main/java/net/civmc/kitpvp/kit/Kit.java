package net.civmc.kitpvp.kit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record Kit(
    int id,
    String name,
    boolean isPublic,
    Material icon,
    ItemStack[] items
) {
    public static boolean checkValidName(Player player, String name) {
        if (name.isBlank() || name.length() > 64) {
            player.sendMessage(Component.text("Kit name too long", NamedTextColor.RED));
            return false;
        } else if (!name.matches("^[A-z0-9_-]+$")) {
            player.sendMessage(Component.text("Kit name contains invalid characters", NamedTextColor.RED));
            return false;
        } else if (name.equalsIgnoreCase("public")) {
            player.sendMessage(Component.text("Kit cannot be called 'public'", NamedTextColor.RED));
            return false;
        }
        return true;
    }
}
