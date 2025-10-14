package com.github.igotyou.FactoryMod;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;

public interface FMItems {

    CustomItemFactory BASTION_CASING = CustomItem.registerCustomItem("bastion_casing", () -> {
        final ItemStack casing = ItemStack.of(Material.ENDER_PEARL);
        casing.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Casing</i>"));
        casing.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Casing for bastion")
        )));
        casing.setAmount(1);
        return casing;
    });

    CustomItemFactory BASTION_GEARBOX = CustomItem.registerCustomItem("bastion_gearbox", () -> {
        final ItemStack gearbox = ItemStack.of(Material.CLOCK);
        gearbox.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Gearbox</i>"));
        gearbox.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Gear mechanism for bastion")
        )));
        gearbox.setAmount(1);
        return gearbox;
    });

    CustomItemFactory BASTION_RADAR = CustomItem.registerCustomItem("bastion_radar", () -> {
        final ItemStack radar = ItemStack.of(Material.COMPASS);
        radar.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Radar</i>"));
        radar.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Radar for detecting pearls")
        )));
        radar.setAmount(1);
        return radar;
    });

    CustomItemFactory BASTION_ENERGIZER = CustomItem.registerCustomItem("bastion_energizer", () -> {
        final ItemStack energizer = ItemStack.of(Material.EMERALD);
        energizer.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Energizer</i>"));
        energizer.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Power source for bastion")
        )));
        energizer.setAmount(1);
        return energizer;
    });

    CustomItemFactory BASTION_REFRACTOR = CustomItem.registerCustomItem("bastion_refractor", () -> {
        final ItemStack refractor = ItemStack.of(Material.DIAMOND);
        refractor.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Refractor</i>"));
        refractor.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Refractor for bastion")
        )));
        refractor.setAmount(1);
        return refractor;
    });

    CustomItemFactory BASTION_BIO_COMPONENT = CustomItem.registerCustomItem("bastion_bio_component", () -> {
        final ItemStack bioComponent = ItemStack.of(Material.MAGMA_CREAM);
        bioComponent.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Bio-Component</i>"));
        bioComponent.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Bio-Component for bastion")
        )));
        bioComponent.setAmount(1);
        return bioComponent;
    });

    /**
     * Register custom items for use in the config
     */
    static void registerCustomItems() {
        // ensure items are registered
        BASTION_CASING.createItem();
        BASTION_GEARBOX.createItem();
        BASTION_RADAR.createItem();
        BASTION_ENERGIZER.createItem();
        BASTION_REFRACTOR.createItem();
        BASTION_BIO_COMPONENT.createItem();
    }
}
