package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import vg.civcraft.mc.civmodcore.inventory.items.updater.listeners.UpdateInventoryItemsOnOpen;

public class ItemMetaConverterHack extends BasicHack implements UpdateInventoryItemsOnOpen {
    private static final ItemStack BAD_CRATE;
    private static final ItemMeta GOOD_CRATE;

    static {
        BAD_CRATE = new ItemStack(Material.CHEST); {
            final ItemMeta meta = BAD_CRATE.getItemMeta();
            meta.lore(List.of(Component.empty().append(Component.text("Crate"))));
            BAD_CRATE.setItemMeta(meta);
        }

        GOOD_CRATE = BAD_CRATE.getItemMeta();
        GOOD_CRATE.lore(List.of(Component.text("Crate")));
    }

    public ItemMetaConverterHack(
        final @NotNull SimpleAdminHacks plugin,
        final @NotNull BasicHackConfig config
    ) {
        super(plugin, config);
    }

    @Override
    public void updateItem(
        final @NotNull ItemStack item
    ) {
        if (item.isSimilar(BAD_CRATE)) {
            item.setItemMeta(GOOD_CRATE);
        }
    }
}
