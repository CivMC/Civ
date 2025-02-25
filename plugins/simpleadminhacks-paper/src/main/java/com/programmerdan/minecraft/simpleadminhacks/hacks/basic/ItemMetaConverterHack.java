package com.programmerdan.minecraft.simpleadminhacks.hacks.basic;

import com.programmerdan.minecraft.simpleadminhacks.SimpleAdminHacks;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHack;
import com.programmerdan.minecraft.simpleadminhacks.framework.BasicHackConfig;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.chat.ChatUtils;
import java.util.Collections;
import java.util.List;

public class ItemMetaConverterHack extends BasicHack {

    private static final ItemStack BAD_CRATE;
    private static final ItemStack GOOD_CRATE;

    static {
        BAD_CRATE = new ItemStack(Material.CHEST);
        ItemMeta badCrateMeta = BAD_CRATE.getItemMeta();
        badCrateMeta.lore(Collections.singletonList(Component.empty().append(Component.text("Crate"))));
        BAD_CRATE.setItemMeta(badCrateMeta);

        GOOD_CRATE = new ItemStack(Material.CHEST);
        ItemMeta goodCrateMeta = BAD_CRATE.getItemMeta();
        goodCrateMeta.lore(Collections.singletonList(Component.text("Crate")));
        GOOD_CRATE.setItemMeta(goodCrateMeta);
    }

    public ItemMetaConverterHack(final SimpleAdminHacks plugin, final BasicHackConfig config) {
        super(plugin, config);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void convertOnInventoryOpen(final InventoryOpenEvent event) {
        final var inventory = event.getInventory();
        if (inventory.getHolder() == null) {
            return; // GUI
        }
        for (final ItemStack item : inventory.getStorageContents()) {
            processItem(item);
        }
    }

    public static void processItem(final ItemStack item) {
        if (item == null) {
            return;
        }
        if (item.isSimilar(BAD_CRATE)) {
            item.setItemMeta(GOOD_CRATE.getItemMeta());
        }
    }
}
