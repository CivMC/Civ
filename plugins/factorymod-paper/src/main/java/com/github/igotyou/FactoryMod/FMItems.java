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

    CustomItemFactory RAIL_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("rail_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Rail Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Rail Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory REDSTONE_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("redstone_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Redstone Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Redstone Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory BASTION_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("bastion_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Bastion Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Bastion Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory IRON_FORGE_REPAIR_KIT = CustomItem.registerCustomItem("iron_forge_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Iron Forge Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair an Iron Forge to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory ANIMAL_HUSBANDRY_REPAIR_KIT = CustomItem.registerCustomItem("animal_husbandry_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Animal Husbandry Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair an Animal Husbandry to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory BASIC_CAULDRON_REPAIR_KIT = CustomItem.registerCustomItem("basic_cauldron_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Basic Cauldron Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Basic Cauldron to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory ADVANCED_CAULDRON_REPAIR_KIT = CustomItem.registerCustomItem("advanced_cauldron_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Advanced Cauldron Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Advanced Cauldron to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory COMPACT_REPAIR_KIT = CustomItem.registerCustomItem("compact_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Compact Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a compactor to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory PRINTER_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("printer_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Printer Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Printer Factory to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory RESEARCH_STATION_REPAIR_KIT = CustomItem.registerCustomItem("research_station_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Research Station Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Research Station to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory COPPER_WORKSHOP_REPAIR_KIT = CustomItem.registerCustomItem("copper_workshop_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Copper Workshop Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Copper Workshop to full health.")
        )));
        repairKit.setAmount(1);
        return repairKit;
    });

    CustomItemFactory BIO_FACTORY_REPAIR_KIT = CustomItem.registerCustomItem("bio_factory_repair_kit", () -> {
        final ItemStack repairKit = ItemStack.of(Material.BARREL);
        repairKit.setData(DataComponentTypes.ITEM_NAME, Component.text("Bio Factory Repair Kit"));
        repairKit.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(
            Component.text("One kit will repair a Bio Factory to full health.")
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
        RAIL_FACTORY_REPAIR_KIT.createItem();
        REDSTONE_FACTORY_REPAIR_KIT.createItem();
        BASTION_FACTORY_REPAIR_KIT.createItem();
        IRON_FORGE_REPAIR_KIT.createItem();
        ANIMAL_HUSBANDRY_REPAIR_KIT.createItem();
        BASIC_CAULDRON_REPAIR_KIT.createItem();
        ADVANCED_CAULDRON_REPAIR_KIT.createItem();
        COMPACT_REPAIR_KIT.createItem();
        PRINTER_FACTORY_REPAIR_KIT.createItem();
        RESEARCH_STATION_REPAIR_KIT.createItem();
        COPPER_WORKSHOP_REPAIR_KIT.createItem();
        BIO_FACTORY_REPAIR_KIT.createItem();
    }
}
