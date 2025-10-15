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

    CustomItemFactory CRATE = CustomItem.registerCustomItem("crate", () -> {
        final ItemStack crate = ItemStack.of(Material.CHEST);
        crate.setData(DataComponentTypes.ITEM_NAME, MiniMessage.miniMessage().deserialize("<i>Crate</i>"));
        crate.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("Used to compact items")
        )));
        crate.setAmount(1);
        return crate;
    });

    CustomItemFactory GEM_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("gem_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Gem Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Gem Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory CONCRETE_MIXER_REPAIR_KIT = CustomItem.registerCustomItem("concrete_mixer_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Concrete Mixer Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Concrete Mixer to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory GRILL_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("grill_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Grill Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Grill Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory ORE_SMELTER_REPAIR_KIT = CustomItem.registerCustomItem("ore_smelter_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Ore Smelter Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair an Ore Smelter to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory ADVANCED_ORE_SMELTER_REPAIR_KIT = CustomItem.registerCustomItem("advanced_ore_smelter_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Advanced Ore Smelter Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair an Advanced Ore Smelter to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory ELITE_ORE_SMELTER_REPAIR_KIT = CustomItem.registerCustomItem("elite_ore_smelter_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Elite Ore Smelter Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair an Elite Ore Smelter to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
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
        CRATE.createItem();
        GEM_FACTORY_REPAIR_KIT.createItem();
        CONCRETE_MIXER_REPAIR_KIT.createItem();
        GRILL_FACTORY_REPAIR_KIT.createItem();
        ORE_SMELTER_REPAIR_KIT.createItem();
        ADVANCED_ORE_SMELTER_REPAIR_KIT.createItem();
        ELITE_ORE_SMELTER_REPAIR_KIT.createItem();
    }
}
