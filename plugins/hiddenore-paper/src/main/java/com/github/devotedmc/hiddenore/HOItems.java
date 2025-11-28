package com.github.devotedmc.hiddenore;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;
import java.util.List;

public interface HOItems {

    CustomItemFactory FOSSIL = CustomItem.registerCustomItem("fossil", () -> {
        final ItemStack repairKit = ItemStack.of(Material.PRISMARINE_SHARD);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Fossil"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Crack me in a factory for a prize!")
        )));
        repairKit.setData(DataComponentTypes.RARITY, ItemRarity.EPIC);
        repairKit.setAmount(1);
        return repairKit;
    });

    /**
     * Register custom items for use in the config
     */
    static void registerCustomItems() {
        FOSSIL.createItem();
    }
}
