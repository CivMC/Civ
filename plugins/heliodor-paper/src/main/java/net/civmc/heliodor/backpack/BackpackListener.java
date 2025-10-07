package net.civmc.heliodor.backpack;

import net.civmc.heliodor.HeliodorPlugin;
import net.kyori.adventure.text.Component;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.EnderChest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public class BackpackListener implements Listener {

    public static final NamespacedKey BACKPACK_ENDER_CHEST_KEY = new NamespacedKey(JavaPlugin.getPlugin(HeliodorPlugin.class), "backpack");

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (!Backpack.isBackpack(item)) {
            return;
        }

        Block block = event.getBlock();
        EnderChest type = (EnderChest) block.getState(false);

        type.getPersistentDataContainer().set(BACKPACK_ENDER_CHEST_KEY, PersistentDataType.BOOLEAN, true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() != Material.ENDER_CHEST) {
            return;
        }

        EnderChest state = (EnderChest) block.getState(false);

        if (!state.getPersistentDataContainer().has(BACKPACK_ENDER_CHEST_KEY)) {
            return;
        }

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), Backpack.createBackpack());
    }

    @EventHandler
    public void on(PlayerInteractEvent event) {
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.ENDER_CHEST || action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        EnderChest state = (EnderChest) block.getState(false);

        if (!state.getPersistentDataContainer().has(BACKPACK_ENDER_CHEST_KEY)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(InventoryOpenEvent event) {
        if (!isBackpackInventory(event.getInventory())) {
            return;
        }

        event.titleOverride(Component.text("Backpack"));
    }

    @EventHandler
    public void on(InventoryClickEvent event) {
        if (!isBackpackInventory(event.getInventory())) {
            return;
        }

        switch (event.getAction()) {
            case PLACE_ALL, PLACE_ONE, PLACE_SOME, SWAP_WITH_CURSOR, DROP_ALL_CURSOR, DROP_ONE_CURSOR -> {
                if (event.getInventory() == event.getClickedInventory() && isIllegalItem(event.getCursor())) {
                    event.setCancelled(true);
                }
            }
            case MOVE_TO_OTHER_INVENTORY -> {
                if (event.getInventory() != event.getClickedInventory() && isIllegalItem(event.getCurrentItem())) {
                    event.setCancelled(true);
                }
            }
            case HOTBAR_SWAP -> {
                if (isIllegalItem(event.getWhoClicked().getInventory().getStorageContents()[event.getHotbarButton()])) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void on(InventoryDragEvent event) {
        if (!isBackpackInventory(event.getInventory())) {
            return;
        }

        if (isIllegalItem(event.getOldCursor())) {
            for (int slot : event.getRawSlots()) {
                if (slot < 27) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void on(PlayerDeathEvent event) {
        if (Bukkit.getPluginManager().isPluginEnabled("CombatTagPlus") && !JavaPlugin.getPlugin(CombatTagPlus.class).getTagManager().isTagged(event.getPlayer().getUniqueId())) {
            return;
        }

        Inventory enderChest = event.getPlayer().getEnderChest();
        for (ItemStack item : enderChest.getStorageContents()) {
            if (item != null && !item.isEmpty()) {
                event.getDrops().add(item);
            }
        }
        enderChest.clear();
    }

    private boolean isBackpackInventory(Inventory inventory) {
        if (inventory.getLocation() == null) {
            return false;
        }

        Block block = inventory.getLocation().getBlock();
        if (block.getType() != Material.ENDER_CHEST) {
            return false;
        }

        if (!((EnderChest) block.getState(false)).getPersistentDataContainer().has(BACKPACK_ENDER_CHEST_KEY)) {
            return false;
        }
        return true;
    }

    private boolean isIllegalItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return false;
        }
        switch (item.getType()) {
            case OBSIDIAN,
                CRYING_OBSIDIAN,
                RESPAWN_ANCHOR,
                ENDER_PEARL,
                NETHERITE_BLOCK,
                ENDER_CHEST,
                GOLDEN_APPLE,
                ENCHANTED_GOLDEN_APPLE,
                ANCIENT_DEBRIS,
                COBWEB,
                CHORUS_FRUIT:
                return true;
        }
        if (item.getMaxStackSize() == 1) {
            return true;
        }
        return false;
    }
}
