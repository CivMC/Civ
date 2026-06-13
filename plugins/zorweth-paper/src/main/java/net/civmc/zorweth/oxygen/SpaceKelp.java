package net.civmc.zorweth.oxygen;

import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class SpaceKelp {

    private SpaceKelp() {
    }

    public static ItemStack create(final int amount) {
        final ItemStack item = new ItemStack(Material.KELP, amount);
        item.editMeta(meta -> {
            meta.displayName(Component.text("Space Kelp"));
            meta.lore(List.of(Component.text("Kelp from Zorweth")));
        });
        return item;
    }
}
