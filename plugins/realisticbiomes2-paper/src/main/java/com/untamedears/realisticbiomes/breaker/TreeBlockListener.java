package com.untamedears.realisticbiomes.breaker;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItem;
import vg.civcraft.mc.civmodcore.inventory.items.custom.CustomItemFactory;

public class TreeBlockListener implements Listener {
    private static final CustomItemFactory WOOD_SCRAP = CustomItem.registerCustomItem("wood_scrap", () -> {
        final ItemStack item = ItemStack.of(Material.OAK_PRESSURE_PLATE);
        final ItemMeta meta = item.getItemMeta();
        meta.itemName(Component.text("Wood scrap", TextColor.color(0xbc7020)));
        meta.lore(List.of(
            Component.text("Leftover scrap wood from cutting down a tree", NamedTextColor.WHITE),
            Component.text("Convert into chests in a Carpentry Factory", NamedTextColor.WHITE)
        ));
        item.setItemMeta(meta);
        return item;
    });

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (!Tag.LOGS.isTagged(block.getType())) {
            return;
        }

        if (!TreeDelegate.remove(block)) {
            return;
        }

        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
        if (item.getType() != Material.NETHERITE_AXE) {
            return;
        }

        if (ThreadLocalRandom.current().nextInt(5) == 0) {
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), WOOD_SCRAP.createItem());
        }
    }
}
